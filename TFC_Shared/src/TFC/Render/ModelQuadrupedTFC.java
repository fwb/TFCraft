package TFC.Render;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.entity.*;
import net.minecraft.client.gui.inventory.*;
import net.minecraft.client.model.*;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.entity.*;
import net.minecraft.block.*;
import net.minecraft.block.material.*;
import net.minecraft.crash.*;
import net.minecraft.creativetab.*;
import net.minecraft.entity.*;
import net.minecraft.entity.ai.*;
import net.minecraft.entity.effect.*;
import net.minecraft.entity.item.*;
import net.minecraft.entity.monster.*;
import net.minecraft.entity.player.*;
import net.minecraft.entity.projectile.*;
import net.minecraft.inventory.*;
import net.minecraft.item.*;
import net.minecraft.nbt.*;
import net.minecraft.network.*;
import net.minecraft.network.packet.*;
import net.minecraft.pathfinding.*;
import net.minecraft.potion.*;
import net.minecraft.server.*;
import net.minecraft.stats.*;
import net.minecraft.tileentity.*;
import net.minecraft.util.*;
import net.minecraft.village.*;
import net.minecraft.world.*;
import org.lwjgl.opengl.GL11;
import TFC.*;
import TFC.Core.TFC_Settings;
import TFC.Entities.EntityAnimalTFC;

public class ModelQuadrupedTFC extends ModelBaseTFC
{
    public ModelRenderer head = new ModelRenderer(this, 0, 0);
    public ModelRenderer body;
    public ModelRenderer leg1;
    public ModelRenderer leg2;
    public ModelRenderer leg3;
    public ModelRenderer leg4;
    protected float field_40331_g = 8.0F;
    protected float field_40332_n = 4.0F;

    public ModelQuadrupedTFC(int par1, float par2)
    {
        this.head.addBox(-4.0F, -4.0F, -8.0F, 8, 8, 8, par2);
        this.head.setRotationPoint(0.0F, (float)(18 - par1), -6.0F);
        this.body = new ModelRenderer(this, 28, 8);
        this.body.addBox(-5.0F, -10.0F, -7.0F, 10, 16, 8, par2);
        this.body.setRotationPoint(0.0F, (float)(17 - par1), 2.0F);
        this.leg1 = new ModelRenderer(this, 0, 16);
        this.leg1.addBox(-2.0F, 0.0F, -2.0F, 4, par1, 4, par2);
        this.leg1.setRotationPoint(-3.0F, (float)(24 - par1), 7.0F);
        this.leg2 = new ModelRenderer(this, 0, 16);
        this.leg2.addBox(-2.0F, 0.0F, -2.0F, 4, par1, 4, par2);
        this.leg2.setRotationPoint(3.0F, (float)(24 - par1), 7.0F);
        this.leg3 = new ModelRenderer(this, 0, 16);
        this.leg3.addBox(-2.0F, 0.0F, -2.0F, 4, par1, 4, par2);
        this.leg3.setRotationPoint(-3.0F, (float)(24 - par1), -5.0F);
        this.leg4 = new ModelRenderer(this, 0, 16);
        this.leg4.addBox(-2.0F, 0.0F, -2.0F, 4, par1, 4, par2);
        this.leg4.setRotationPoint(3.0F, (float)(24 - par1), -5.0F);
    }

    /**
     * Sets the models various rotation angles then renders the model.
     */
    public void render(Entity par1Entity, float par2, float par3, float par4, float par5, float par6, float par7)
    {
        this.setRotationAngles(par2, par3, par4, par5, par6, par7);
        age = 0;
        if (par1Entity instanceof EntityAnimalTFC){
        	if(((EntityAnimalTFC)par1Entity).getGrowingAge() < 0)
        	age = (-1F)*((EntityAnimalTFC)par1Entity).getGrowingAge() / (((EntityAnimalTFC)par1Entity).adultAge * TFC_Settings.dayLength);
        }
        if (true)//this.isChild)
        {
            float aa =  2F - (1.0F - age);
            GL11.glPushMatrix ();
            float ab = (float)Math.sqrt(1.0F / aa);
            GL11.glScalef(ab, ab, ab);
            GL11.glTranslatef (0.0F, 24F * par7 * age/aa,2F*par7*age/ab);            
            head.render(par7);
            GL11.glPopMatrix();
            GL11.glPushMatrix();
            GL11.glScalef(1.0F / aa, 1.0F / aa, 1.0F / aa);
            GL11.glTranslatef(0.0F, 24F * par7 * age, 0.0F);
            body.render(par7);
            leg1.render(par7);
            leg2.render(par7);
            leg3.render(par7);
            leg4.render(par7);
            GL11.glPopMatrix();
        }
        else
        {
            this.head.render(par7);
            this.body.render(par7);
            this.leg1.render(par7);
            this.leg2.render(par7);
            this.leg3.render(par7);
            this.leg4.render(par7);
        }
    }

    /**
     * Sets the models various rotation angles.
     */
    public void setRotationAngles(float par1, float par2, float par3, float par4, float par5, float par6)
    {
        this.head.rotateAngleX = par5 / (180F / (float)Math.PI);
        this.head.rotateAngleY = par4 / (180F / (float)Math.PI);
        this.body.rotateAngleX = ((float)Math.PI / 2F);
        this.leg1.rotateAngleX = MathHelper.cos(par1 * 0.6662F) * 1.4F * par2;
        this.leg2.rotateAngleX = MathHelper.cos(par1 * 0.6662F + (float)Math.PI) * 1.4F * par2;
        this.leg3.rotateAngleX = MathHelper.cos(par1 * 0.6662F + (float)Math.PI) * 1.4F * par2;
        this.leg4.rotateAngleX = MathHelper.cos(par1 * 0.6662F) * 1.4F * par2;
    }
}
