package ru.liahim.mist.api.event;

import net.minecraft.entity.EntityLivingBase;
import net.minecraftforge.event.entity.living.LivingEvent;

public class PollutionEvent extends LivingEvent {
    private float pollution;
    private float toxic;
    private float fogDamage;
    private float rainDamage;

    public PollutionEvent(EntityLivingBase entity, float pollution, float toxic, float fogDamage, float rainDamage) {
        super(entity);
        this.pollution = pollution;
        this.toxic = toxic;
        this.fogDamage = fogDamage;
        this.rainDamage = rainDamage;
    }

    public float getPollution() {
        return pollution;
    }

    public void setPollution(float pollution) {
        this.pollution = pollution;
    }

    public float getToxic() {
        return toxic;
    }

    public void setToxic(float toxic) {
        this.toxic = toxic;
    }

    public float getFogDamage() {
        return fogDamage;
    }

    public void setFogDamage(float fogDamage) {
        this.fogDamage = fogDamage;
    }

    public float getRainDamage() {
        return rainDamage;
    }

    public void setRainDamage(float rainDamage) {
        this.rainDamage = rainDamage;
    }
}
