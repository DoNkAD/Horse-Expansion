package net.donkad.horse.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.passive.AbstractDonkeyEntity;
import net.minecraft.entity.passive.AbstractHorseEntity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractHorseEntity.class)
public abstract class AbstractHorseMixin extends AnimalEntity {
    @Shadow private float lastAngryAnimationProgress;
    public String secondPassenger = "SecondPassenger";

    protected AbstractHorseMixin(EntityType<? extends AnimalEntity> entityType, World world) {
        super(entityType, world);
    }

    @Inject(method = "updatePassengerPosition", at = @At("TAIL"))
    private void updatePassengerPositionMixin(Entity passenger, CallbackInfo ci){
        if (this.hasPassenger(passenger)){
            int j = passenger.getId() % 2 == 0 ? 90 : 270;
            float f = 0.0f;
            float g = (float)((this.isRemoved() ? 0.009999999776482582D : this.getMountedHeightOffset()) + passenger.getHeightOffset());
            if (this.getPassengerList().size() > 1) {
                int i = this.getPassengerList().indexOf(passenger);
                if (i == 0) {
                    f = 0.2F;
                } else {
                    f = -0.6F;
                }
            }

            Vec3d vec3d = (new Vec3d(f, 0.0D, 0.0D)).rotateY(-this.getYaw() * 0.017453292F - 1.5707964F);
            passenger.setPosition(this.getX() + vec3d.x, this.getY() + (double)g, this.getZ() + vec3d.z);
            if (passenger instanceof AnimalEntity && this.getPassengerList().size() == this.getMaxPassengers()) {
                passenger.setBodyYaw(((AnimalEntity)passenger).bodyYaw + (float)j);
                passenger.setHeadYaw(passenger.getHeadYaw() + (float)j);
            }

            if (lastAngryAnimationProgress > 0.0f){
                float a = MathHelper.sin(this.bodyYaw * 0.017453292F);
                float b = MathHelper.cos(this.bodyYaw * 0.017453292F);
                float h = 0.7F * this.lastAngryAnimationProgress;
                float i = 0.15F * this.lastAngryAnimationProgress;
                passenger.setPosition(this.getX() + (double)(h * a), this.getY() + this.getMountedHeightOffset() + passenger.getHeightOffset() + (double)i, this.getZ() - (double)(h * b));

                if (passenger != this.getPrimaryPassenger()){
                    passenger.addScoreboardTag(secondPassenger);
                    if (passenger.getScoreboardTags().contains(secondPassenger)) {
                        passenger.setPosition(
                                this.getX() + vec3d.x - (double)(h * a),
                                this.getY() + (double)g,
                                this.getZ() - (double)(h * b) + vec3d.z);
                    }
                }else if (passenger == this.getPrimaryPassenger()){
                    passenger.removeScoreboardTag(secondPassenger);
                }

            }
        }
    }

    protected boolean canAddPassenger(Entity passenger) {
        return this.getPassengerList().size() < this.getMaxPassengers();
    }

    protected int getMaxPassengers() {
        return 2;
    }

    @Nullable
    public Entity getPrimaryPassenger() {
        return this.getFirstPassenger();
    }

    @Override
    public ActionResult interactMob(PlayerEntity player, Hand hand) {
        if (!this.world.isClient){
            if (this.getPassengerList().size() < this.getMaxPassengers()){
                player.startRiding(this);
            }
        }
        return super.interactMob(player, hand);
    }
}
