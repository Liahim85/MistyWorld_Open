package ru.liahim.mist.shader;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.util.ResourceLocation;
import ru.liahim.mist.common.Mist;
import ru.liahim.mist.handlers.FogRenderer;
import ru.liahim.mist.init.ModConfig;
import ru.liahim.mist.util.FogTexture;

public class ShaderProgram {

	private static int current = 0;
	public static int fog = 0;

	public static void initShaders() {
		if(!useShaders()) return;
		fog = createProgram("fog", "fog");
	}

	public static void useShader(int shader) {
		if(!useShaders()) return;
		GL20.glUseProgram(shader);
		current = shader;
		if(shader != 0) {
			int mode = GL20.glGetUniformLocation(shader, "fog_mode");
			GL20.glUniform1i(mode, FogRenderer.depth < 0 ? 1 : 0);
			int density = GL20.glGetUniformLocation(shader, "fog_density");
			GL20.glUniform1f(density, FogRenderer.density);
			int fog_color = GL20.glGetUniformLocation(shader, "fog_color");
			GL20.glUniform3f(fog_color, FogRenderer.red, FogRenderer.green, FogRenderer.blue);
			int tex_size = GL20.glGetUniformLocation(shader, "tex_size");
			GL20.glUniform2f(tex_size, FogTexture.getTextureSize(), FogTexture.getTextureSize());
		}
	}

	public static void setUniform1f(String name, float v) {
		if (current != 0) {
			int id = GL20.glGetUniformLocation(current, name);
			GL20.glUniform1f(id, v);
		}
	}

	public static void setUniform2f(String name, float v0, float v1) {
		if (current != 0) {
			int id = GL20.glGetUniformLocation(current, name);
			GL20.glUniform2f(id, v0, v1);
		}
	}

	public static void setUniform3f(String name, float v0, float v1, float v2) {
		if (current != 0) {
			int id = GL20.glGetUniformLocation(current, name);
			GL20.glUniform3f(id, v0, v1, v2);
		}
	}

	public static void releaseShader() {
		useShader(0);
	}

	private static boolean useShaders() {
		return ModConfig.graphic.advancedFogRenderer && OpenGlHelper.shadersSupported;
	}

	private static int createProgram(String vert, String frag) {
		int vertId = 0, fragId = 0, program = 0;

		if(vert != null) vertId = createShader(vert + ".vert", GL20.GL_VERTEX_SHADER);
		if(frag != null) fragId = createShader(frag + ".frag", GL20.GL_FRAGMENT_SHADER);

		program = GL20.glCreateProgram();
		if(program == 0) return 0;

		if(vert != null) GL20.glAttachShader(program, vertId);
		if(frag != null) GL20.glAttachShader(program, fragId);

		GL20.glLinkProgram(program);
		if (GL20.glGetProgrami(program, GL20.GL_LINK_STATUS) == GL11.GL_FALSE) {
            System.err.println("Link program error: " + GL20.glGetProgramInfoLog(program, GL20.GL_INFO_LOG_LENGTH));
            return 0;
		}
		GL20.glValidateProgram(program);
		if (GL20.glGetProgrami(program, GL20.GL_VALIDATE_STATUS) == GL11.GL_FALSE) {
            System.err.println("Validate program error: " + GL20.glGetProgramInfoLog(program, GL20.GL_INFO_LOG_LENGTH));
            return 0;
		}
		return program;
	}
	
	private static int createShader(String filename, int shaderType) {
		int shader = 0;
		try {
			shader = GL20.glCreateShader(shaderType);
			if (shader == 0) return 0;
			GL20.glShaderSource(shader, readFileAsString(filename));
			GL20.glCompileShader(shader);
			if (GL20.glGetShaderi(shader, GL20.GL_COMPILE_STATUS) == GL11.GL_FALSE)
				throw new RuntimeException("Error creating shader: " + GL20.glGetShaderInfoLog(shader, GL20.GL_INFO_LOG_LENGTH));
			return shader;
		} catch(Exception e) {
			GL20.glDeleteShader(shader);
			e.printStackTrace();
			return -1;
		}
	}
	
	private static String readFileAsString(String filename) throws Exception {
		StringBuilder source = new StringBuilder();
        ResourceLocation rs = new ResourceLocation(Mist.MODID, "shaders/" + filename);
        InputStream in = Minecraft.getMinecraft().getResourceManager().getResource(rs).getInputStream();
		Exception exception = null;
		BufferedReader reader;
		if(in == null) return "";
		try {
			reader = new BufferedReader(new InputStreamReader(in, "UTF-8"));
			Exception innerExc = null;
			try {
				String line;
				while((line = reader.readLine()) != null) source.append(line).append('\n');
			} catch(Exception exc) {
				exception = exc;
			} finally {
				try {
					reader.close();
				} catch(Exception exc) {
					if(innerExc == null) innerExc = exc;
					else exc.printStackTrace();
				}
			}
			if(innerExc != null) throw innerExc;
		} catch(Exception exc) {
			exception = exc;
		} finally {
			try {
				in.close();
			} catch(Exception exc) {
				if(exception == null) exception = exc;
				else exc.printStackTrace();
			}
			if(exception != null) throw exception;
		}
		return source.toString();
	}
}