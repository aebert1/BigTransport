package net.minecraft.client.particle;

import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class EntityFlameFX extends EntityFX
{
    /** the scale of the flame FX */
    private float flameScale;

    protected EntityFlameFX(World worldIn, double xCoordIn, double yCoordIn, double zCoordIn, double xSpeedIn, double ySpeedIn, double zSpeedIn)
    {
        super(worldIn, xCoordIn, yCoordIn, zCoordIn, xSpeedIn, ySpeedIn, zSpeedIn);
        this.xSpeed = this.xSpeed * 0.009999999776482582D + xSpeedIn;
        this.ySpeed = this.ySpeed * 0.009999999776482582D + ySpeedIn;
        this.zSpeed = this.zSpeed * 0.009999999776482582D + zSpeedIn;
        this.posX += (double)((this.rand.nextFloat() - this.rand.nextFloat()) * 0.05F);
        this.posY += (double)((this.rand.nextFloat() - this.rand.nextFloat()) * 0.05F);
        this.posZ += (double)((this.rand.nextFloat() - this.rand.nextFloat()) * 0.05F);
        this.flameScale = this.particleScale;
        this.particleRed = this.particleGreen = this.particleBlue = 1.0F;
        this.particleMaxAge = (int)(8.0D / (Math.random() * 0.8D + 0.2D)) + 4;
        this.setParticleTextureIndex(48);
    }

    public void moveEntity(double x, double y, double z)
    {
        this.setEntityBoundingBox(this.getEntityBoundingBox().offset(x, y, z));
        this.resetPositionToBB();
    }

    /**
     * Renders the particle
     */
    public void renderParticle(VertexBuffer worldRendererIn, Entity entityIn, float partialTicks, float rotationX, float rotationZ, float rotationYZ, float rotationXY, float rotationXZ)
    {
        float f = ((float)this.particleAge + partialTicks) / (float)this.particleMaxAge;
        this.particleScale = this.flameScale * (1.0F - f * f * 0.5F);
        super.renderParticle(worldRendererIn, entityIn, partialTicks, rotationX, rotationZ, rotationYZ, rotationXY, rotationXZ);
    }

    public int getBrightnessForRender(float p_189214_1_)
    {
        float f = ((float)this.particleAge + p_189214_1_) / (float)this.particleMaxAge;
        f = MathHelper.clamp_float(f, 0.0F, 1.0F);
        int i = super.getBrightnessForRender(p_189214_1_);
        int j = i & 255;
        int k = i >> 16 & 255;
        j = j + (int)(f * 15.0F * 16.0F);

        if (j > 240)
        {
            j = 240;
        }

        return j | k << 16;
    }

    public void onUpdate()
    {
        this.prevPosX = this.posX;
        this.prevPosY = this.posY;
        this.prevPosZ = this.posZ;

        if (this.particleAge++ >= this.particleMaxAge)
        {
            this.setExpired();
        }

        this.moveEntity(this.xSpeed, this.ySpeed, this.zSpeed);
        this.xSpeed *= 0.9599999785423279D;
        this.ySpeed *= 0.9599999785423279D;
        this.zSpeed *= 0.9599999785423279D;

        if (this.isCollided)
        {
            this.xSpeed *= 0.699999988079071D;
            this.zSpeed *= 0.699999988079071D;
        }
    }

    @SideOnly(Side.CLIENT)
    public static class Factory implements IParticleFactory
        {
            public EntityFX getEntityFX(int particleID, World worldIn, double xCoordIn, double yCoordIn, double zCoordIn, double xSpeedIn, double ySpeedIn, double zSpeedIn, int... p_178902_15_)
            {
                return new EntityFlameFX(worldIn, xCoordIn, yCoordIn, zCoordIn, xSpeedIn, ySpeedIn, zSpeedIn);
            }
        }
}