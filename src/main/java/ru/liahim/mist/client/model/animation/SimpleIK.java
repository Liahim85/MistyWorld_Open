package ru.liahim.mist.client.model.animation;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.client.model.ModelRenderer;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.math.MathHelper;

/**
 * Creates a system of two bones following the target using the inverse kinematics principle.
 * @author Liahim
 */
public class SimpleIK {

	private final float[] base;
	private final ModelRenderer[] parents;
	private final ModelRenderer bone_1;
	private final ModelRenderer bone_2;
	private final ModelRenderer target;
	private final float[] targetShift;
	private final float length_1;
	private final float length_2;
	/** Initial rotation angles {x, z} */
	private final float[] ang_1;
	private final float[] ang_2;
	/** Total target position relative to base {x, z} */
	private final float[] targetPos;
	/** Elbow direction */
	private final boolean invert;
	/** Rotation axis */
	private final Axis planeAxis;

	/**
	 * Creates a system of two bones following the target using the inverse kinematics principle.<br>
	 * The first bone should not be connected to the parent.<br>
	 * The target should not be connected to the second bone.
	 * @param planeAxis System rotation axis.
	 * @param invert Elbow direction.
	 * @param parents [0...length - 4] array of parent models
	 * @param parents [length - 3...length - 2] two bone models
	 * @param parents [length - 1] target model
	 */
	public SimpleIK(Axis planeAxis, boolean invert, ModelRenderer... parents) {
		this(planeAxis, invert, null, parents);
	}

	/**
	 * Creates a system of two bones following the target using the inverse kinematics principle.<br>
	 * The first bone should not be connected to the parent.<br>
	 * The target should not be connected to the second bone.
	 * @param planeAxis System rotation axis.
	 * @param invert Elbow direction.
	 * @param targetShift Sets the offset vector of the target point.
	 * @param parents [0...length - 4] array of parent models
	 * @param parents [length - 3...length - 2] two bone models
	 * @param parents [length - 1] target model
	 */
	public SimpleIK(Axis planeAxis, boolean invert, @Nullable float[] targetShift, ModelRenderer... parents) {
		this.planeAxis = planeAxis;
		this.invert = invert;
		this.bone_1 = parents[parents.length - 3];
		this.bone_2 = parents[parents.length - 2];
		this.target = parents[parents.length - 1];
		this.targetShift = targetShift;
		this.parents = new ModelRenderer[parents.length - 3];
		for (int i = 0; i < this.parents.length; ++i) this.parents[i] = parents[i];
		float x = this.target.rotationPointX;
		float y = this.target.rotationPointY;
		float z = this.target.rotationPointZ;
		if (this.targetShift != null) {
			x += this.targetShift[0];
			y += this.targetShift[1];
			z += this.targetShift[2];
		}
		this.base = new float[] { this.bone_1.rotationPointX, this.bone_1.rotationPointY, this.bone_1.rotationPointZ };
		this.length_1 = (float) Math.sqrt(this.bone_2.rotationPointX * this.bone_2.rotationPointX + this.bone_2.rotationPointY * this.bone_2.rotationPointY + this.bone_2.rotationPointZ * this.bone_2.rotationPointZ);
		this.length_2 = (float) Math.sqrt(x * x + y * y + z * z);
		this.ang_1 = this.bone_2.rotationPointY == 0 ? new float[] { 0, 0 } : new float[] {(float) Math.atan(this.bone_2.rotationPointZ/this.bone_2.rotationPointY), (float) Math.atan(this.bone_2.rotationPointX/this.bone_2.rotationPointY)};
		this.ang_2 = y == 0 ? new float[] { 0, 0 } : new float[] { (float) Math.atan(z/y), (float) Math.atan(x/y) };
		this.targetPos = new float[] { this.bone_2.rotationPointX + x, this.bone_2.rotationPointZ + z };
	}

	public void rotateBones(float ang) {
		rotateBones(ang, calculateBasePoint());
	}

	public void rotateBones(float ang, float[] base) {
		float x = this.target.rotationPointX;
		float y = this.target.rotationPointY;
		float z = this.target.rotationPointZ;
		if (this.targetShift != null && (this.targetShift[0] != 0 || this.targetShift[1] != 0 || this.targetShift[2] != 0)) {
			float[] targetVec = this.targetShift.clone();
			if (this.target.rotateAngleX != 0) targetVec = rotateX(targetVec, this.target.rotateAngleX);
			if (this.target.rotateAngleY != 0) targetVec = rotateY(targetVec, this.target.rotateAngleY);
			if (this.target.rotateAngleZ != 0) targetVec = rotateZ(targetVec, this.target.rotateAngleZ);
			x += targetVec[0];
			y += targetVec[1];
			z += targetVec[2];
		}
		x -= base[0];
		y -= base[1];
		z -= base[2];
		float length = (float) Math.sqrt(x * x + y * y + z * z);

		if (length > this.length_1 + this.length_2) length = this.length_1 + this.length_2;
		else if (length < Math.abs(this.length_1 - this.length_2)) length = Math.abs(this.length_1 - this.length_2);

		float bone_1_ang = (float) Math.acos((this.length_1 * this.length_1 + length * length - this.length_2 * this.length_2) / (2 * this.length_1 * length));
		float bone_2_ang = (float) Math.PI - (float) Math.acos((this.length_1 * this.length_1 + this.length_2 * this.length_2 - length * length) / (2 * this.length_1 * this.length_2));

		if (Float.isNaN(bone_1_ang)) bone_1_ang = 0;
		if (Float.isNaN(bone_2_ang)) bone_2_ang = 0;

		if (this.invert) {
			bone_1_ang = -bone_1_ang;
			bone_2_ang = -bone_2_ang;
			ang = -ang;
		}

		float planeAngle;
		float[] axis;
		float axisAngle;
		boolean back;
		float[] matrix;

		if (this.planeAxis == Axis.X) {
			back = y < 0;
			if (back) y = -y;
			planeAngle = (float) Math.atan(z/y);
			axis = rotateX(new float[] {x, y, z}, -planeAngle);
			bone_1_ang = bone_1_ang - (float) Math.atan(axis[0]/axis[1]);

			matrix = rotationMatrix(this.targetPos[1] == 0 || axis[1] == 0 ? 0 : (float) -Math.atan(this.targetPos[1]/axis[1]), 0, bone_1_ang + this.ang_1[1], null);
			if (ang != 0) matrix = multiply(rotationMatrix(axis[0], axis[1], axis[2], -ang, null), matrix, null);
			if (back) planeAngle = (float) Math.PI - planeAngle;
			if (planeAngle != 0) matrix = multiply(rotationMatrix(planeAngle, 0, 0, null), matrix, null);
			this.bone_2.rotateAngleZ = -bone_2_ang - this.ang_1[1] + this.ang_2[1];

		} else if (this.planeAxis == Axis.Y) {
			back = x >= 0;
			if (back) x = -x;
			planeAngle = (float) -Math.atan(z/x);
			axis = rotateY(new float[] {x, y, z}, -planeAngle);
			bone_1_ang = (float) (Math.PI/2 + Math.atan(axis[1]/axis[0])) + bone_1_ang;

			matrix = rotationMatrix(this.targetPos[1] == 0 || axis[1] == 0 ? 0 : (float) -Math.atan(this.targetPos[1]/axis[1]), 0, bone_1_ang + this.ang_1[1], null);
			if (ang != 0) matrix = multiply(rotationMatrix(axis[0], axis[1], axis[2], -ang, null), matrix, null);
			if (back) planeAngle = (float) Math.PI - planeAngle;
			if (planeAngle != 0) matrix = multiply(rotationMatrix(0, planeAngle, 0, null), matrix, null);
			this.bone_2.rotateAngleZ = -bone_2_ang - this.ang_1[1] + this.ang_2[1];
			
			/*back = z >= 0;
			if (back) z = -z;
			planeAngle = (float) Math.atan(x/z);
			axis = rotateY(new float[] {x, y, z}, -planeAngle);
			bone_1_ang = -bone_1_ang - (float) (Math.PI/2 + Math.atan(axis[1]/axis[2]));

			matrix = rotationMatrix(bone_1_ang - this.ang_1[0], 0, this.targetPos[0] == 0 || axis[1] == 0 ? 0 : (float) Math.atan(this.targetPos[0]/axis[1]), null);
			if (ang != 0) matrix = multiply(rotationMatrix(axis[0], axis[1], axis[2], ang, null), matrix, null);
			if (back) planeAngle = (float) Math.PI - planeAngle;
			if (planeAngle != 0) matrix = multiply(rotationMatrix(0, planeAngle, 0, null), matrix, null);
			this.bone_2.rotateAngleX = bone_2_ang + this.ang_1[0] - this.ang_2[0];*/

		} else {
			back = y < 0;
			if (back) y = -y;
			planeAngle = (float) -Math.atan(x/y);
			axis = rotateZ(new float[] {x, y, z}, -planeAngle);
			bone_1_ang = (float) Math.atan(axis[2]/axis[1]) - bone_1_ang;

			matrix = rotationMatrix(bone_1_ang - this.ang_1[0], 0, this.targetPos[0] == 0 || axis[1] == 0 ? 0 : (float) Math.atan(this.targetPos[0]/axis[1]), null);
			if (ang != 0) matrix = multiply(rotationMatrix(axis[0], axis[1], axis[2], ang, null), matrix, null);
			if (back) planeAngle = (float) Math.PI - planeAngle;
			if (planeAngle != 0) matrix = multiply(rotationMatrix(0, 0, planeAngle, null), matrix, null);
			this.bone_2.rotateAngleX = bone_2_ang + this.ang_1[0] - this.ang_2[0];
		}

		float[] angles = eulerAngles(matrix, null);

		this.bone_1.rotateAngleX = angles[0];
		this.bone_1.rotateAngleY = angles[1];
		this.bone_1.rotateAngleZ = angles[2];

		this.bone_1.rotationPointX = base[0];
		this.bone_1.rotationPointY = base[1];
		this.bone_1.rotationPointZ = base[2];
	}

	public float[] calculateBasePoint() {
		return calculateBasePoint(this.base, this.parents);
	}

	public float[] calculateBasePoint(@Nonnull float[] target, ModelRenderer... models) {
		float[] vec = target.clone();
		ModelRenderer model;
		for (int i = models.length - 1; i >= 0; --i) {
			model = models[i];
			if (model.rotateAngleX != 0) vec = rotateX(vec, model.rotateAngleX);
			if (model.rotateAngleY != 0) vec = rotateY(vec, model.rotateAngleY);
			if (model.rotateAngleZ != 0) vec = rotateZ(vec, model.rotateAngleZ);
			vec = addVector(vec, model.rotationPointX, model.rotationPointY, model.rotationPointZ);
		}
		return vec;
	}

	public static float[] rotateX(float[] vec, float ang) {
		float cos = MathHelper.cos(ang);
		float sin = MathHelper.sin(ang);
		float y = vec[1] * cos - vec[2] * sin;
		float z = vec[2] * cos + vec[1] * sin;
		vec[1] = y;
		vec[2] = z;
		return vec;
	}

	public static float[] rotateY(float[] vec, float ang) {
		float cos = MathHelper.cos(ang);
		float sin = MathHelper.sin(ang);
		float x = vec[0] * cos + vec[2] * sin;
		float z = vec[2] * cos - vec[0] * sin;
		vec[0] = x;
		vec[2] = z;
		return vec;
	}

	public static float[] rotateZ(float[] vec, float ang) {
		float cos = MathHelper.cos(ang);
		float sin = MathHelper.sin(ang);
		float x = vec[0] * cos - vec[1] * sin;
		float y = vec[1] * cos + vec[0] * sin;
		vec[0] = x;
		vec[1] = y;
		return vec;
	}

	/** Matrix multiplier */
	public static float[] multiply(float[] a, float[] b, float[] out) {
		if (out == null || out.length < 9) out = new float[9];
		float a11 = a[0];
		float a12 = a[1];
		float a13 = a[2];
		float a21 = a[3];
		float a22 = a[4];
		float a23 = a[5];
		float a31 = a[6];
		float a32 = a[7];
		float a33 = a[8];

		float b11 = b[0];
		float b12 = b[1];
		float b13 = b[2];
		float b21 = b[3];
		float b22 = b[4];
		float b23 = b[5];
		float b31 = b[6];
		float b32 = b[7];
		float b33 = b[8];

		out[0] = a11 * b11 + a12 * b21 + a13 * b31;
		out[1] = a11 * b12 + a12 * b22 + a13 * b32;
		out[2] = a11 * b13 + a12 * b23 + a13 * b33;

		out[3] = a21 * b11 + a22 * b21 + a23 * b31;
		out[4] = a21 * b12 + a22 * b22 + a23 * b32;
		out[5] = a21 * b13 + a22 * b23 + a23 * b33;

		out[6] = a31 * b11 + a32 * b21 + a33 * b31;
		out[7] = a31 * b12 + a32 * b22 + a33 * b32;
		out[8] = a31 * b13 + a32 * b23 + a33 * b33;
		return out;
	}

	/** Calculate Euler angles from matrix */
	private static float[] eulerAngles(float[] matrix, float[] out) {
		if (out == null || out.length < 3) out = new float[3];
		float r11 = matrix[0];
		float r12 = matrix[1];
		float r13 = matrix[2];
		float r21 = matrix[3];

		float r31 = matrix[6];
		float r32 = matrix[7];
		float r33 = matrix[8];

		float psi = 0, theta = 0, phi = 0;
		if (r31 != 1 && r31 != -1) {
			theta = (float) -Math.asin(r31);
			float cos = (float) Math.cos(theta);
			psi = (float) Math.atan2(r32/cos, r33/cos);
			phi = (float) Math.atan2(r21/cos, r11/cos);
		} else {
			if (r31 == -1) {
				theta = (float) (Math.PI/2);
				psi = phi + (float) Math.atan2(r12, r13);
			} else {
				theta = (float) (-Math.PI/2);
				psi = -phi + (float) Math.atan2(-r12, -r13);
			}
		}
		out[0] = psi;
	    out[1] = theta;
	    out[2] = phi;
	    return out;
	}

	/** Create matrix from Euler angles */
	private static float[] rotationMatrix(float x, float y, float z, float[] out) {
		if (out == null || out.length < 9) out = new float[9];
		float sinPsi = (float) Math.sin(x);
		float cosPsi = (float) Math.cos(x);

		float sinTht = (float) Math.sin(y);
		float cosTht = (float) Math.cos(y);

		float sinPhi = (float) Math.sin(z);
		float cosPhi = (float) Math.cos(z);

		return copy( out,
                cosTht * cosPhi, sinPsi * sinTht * cosPhi - cosPsi * sinPhi, cosPsi * sinTht * cosPhi + sinPsi * sinPhi,
                cosTht * sinPhi, sinPsi * sinTht * sinPhi + cosPsi * cosPhi, cosPsi * sinTht * sinPhi - sinPsi * cosPhi,
                -sinTht, sinPsi * cosTht, cosPsi * cosTht );
	}

	/** Create matrix from axis and angle */
	private static float[] rotationMatrix(float ux, float uy, float uz, float th, float[] out) {
		if (out == null || out.length < 9) out = new float[9];
		float len = (float) Math.sqrt(ux * ux + uy * uy + uz * uz);
		if (len != 0) {
			ux = ux / len;
			uy = uy / len;
			uz = uz / len;
		} else {
			ux = 1;
			uy = 0;
			uz = 0;
		}
		float cos = (float) Math.cos(th);
		float sin = (float) Math.sin(th);
		float icos = 1 - cos;

		float uxsq = ux * ux;
		float uysq = uy * uy;
		float uzsq = uz * uz;
		return copy( out,
				cos + uxsq * icos, ux * uy * icos - uz * sin, ux * uz * icos + uy * sin,
				uy * ux * icos + uz * sin, cos + uysq * icos, uy * uz * icos - ux * sin,
				uz * ux * icos - uy * sin, uz * uy * icos + ux * sin, cos + uzsq * icos );
	}

	private static float[] copy(float[] out, float a, float b, float c, float d, float e, float f, float g, float h, float i) {
		// Didn't use varargs as they create extra arrays internally...
		out[0] = a;
		out[1] = b;
		out[2] = c;
		out[3] = d;
		out[4] = e;
		out[5] = f;
		out[6] = g;
		out[7] = h;
		out[8] = i;
		return out;
	}

	public static float[] addVector(float[] v1, float[] v2) {
		return addVector(v1, v2[0], v2[1], v2[2]);
	}

	public static float[] addVector(float[] v1, float x, float y, float z) {
		v1[0] += x;
		v1[1] += y;
		v1[2] += z;
		return v1;
	}
}