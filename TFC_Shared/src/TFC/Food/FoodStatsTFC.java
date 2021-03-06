package TFC.Food;

import TFC.*;
import TFC.Core.TFC_Climate;
import TFC.Core.TFC_Time;
import TFC.Core.Player.PlayerManagerTFC;
import TFC.Core.Player.TFC_PlayerServer;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.entity.*;
import net.minecraft.client.gui.inventory.*;
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
import net.minecraft.world.biome.*;
import net.minecraft.world.chunk.*;
import net.minecraft.world.gen.feature.*;

public class FoodStatsTFC extends FoodStats
{
	/** The player's food level. This measures how much food the player can handle.*/
	public float foodLevel = 100;


	/** The player's food saturation. This is how full the player is from the food that they've eaten.*/
	private float foodSaturationLevel = 5.0F;

	/** The player's food exhaustion. This measures the rate of hunger decay. 
	 * When this reaches 4, some of the stored food is consumed by either 
	 * reducing the satiation or the food level.*/
	private float foodExhaustionLevel;

	/** The player's food timer value. */
	private long foodTimer = 0;
	private long foodHealTimer = 0;

	public float waterLevel = (float) (TFC_Time.dayLength*2/10);
	private long waterTimer = 0;

	public FoodStatsTFC()
	{
		waterTimer = TFC_Time.getTotalTicks();
		foodTimer = TFC_Time.getTotalTicks();
		foodHealTimer = TFC_Time.getTotalTicks();
	}

	/**
	 * Handles the food game logic.
	 */
	@Override
	public void onUpdate(EntityPlayer player)
	{
		if(!player.worldObj.isRemote)
		{
			int difficulty = player.worldObj.difficultySetting;
			EntityPlayerMP playermp = (EntityPlayerMP)player;
			TFC_PlayerServer playerserver = (TFC_PlayerServer) playermp.getServerPlayerBase("TFC Player Server");
			
			float temp = TFC_Climate.getHeightAdjustedTemp((int)player.posX, (int)player.posY, (int)player.posZ);

			if (this.foodExhaustionLevel > 4.0F)
			{
				this.foodExhaustionLevel -= 4.0F;

				if (this.foodSaturationLevel > 0.0F)
				{
					this.foodSaturationLevel = Math.max(this.foodSaturationLevel - 1.0F, 0.0F);
				}
				else if (!player.capabilities.isCreativeMode)
				{
					this.foodLevel = Math.max(this.foodLevel - 2, 0);
				}
			}
			
			if (TFC_Time.getTotalTicks() - this.foodTimer >= TFC_Time.hourLength && !player.capabilities.isCreativeMode)
			{
				this.foodTimer += TFC_Time.hourLength;
				if (this.foodSaturationLevel > 0.0F)
				{
					this.foodSaturationLevel = Math.max(this.foodSaturationLevel - 1.0F, 0.0F);
				}
				else if (!player.capabilities.isCreativeMode)
				{
					this.foodLevel = Math.max(this.foodLevel - 1, 0);
				}
			}

			if (TFC_Time.getTotalTicks() - this.foodHealTimer >= TFC_Time.hourLength/2)
			{
				this.foodHealTimer += TFC_Time.hourLength/2;

				if (this.foodLevel >= 25 && playerserver.shouldHeal())
				{
					player.heal((int) ((float)playerserver.getMaxHealth()*0.01f));

					if (this.foodSaturationLevel > 0.0F)
					{
						this.foodSaturationLevel = Math.max(this.foodSaturationLevel - 4.0F, 0.0F);
					}
					else if (!player.capabilities.isCreativeMode)
					{
						this.foodLevel = Math.max(this.foodLevel - 1, 0);
					}
				}
				else if (this.foodLevel <= 0)
				{
					if (difficulty > 1 || (player.getHealth() > 50))
					{
						player.attackEntityFrom(DamageSource.starve, 50);
					}
				}
			}

			float tempWaterMod = temp;
			if(tempWaterMod >= 30)
				tempWaterMod = (tempWaterMod-30)*0.1f;
			else tempWaterMod = 0;
			//Handle water related ticking
			if(player.isSprinting()&& !player.capabilities.isCreativeMode)
			{
				waterLevel -= 1+(tempWaterMod);
			}

			for(;waterTimer <= TFC_Time.getTotalTicks() && !player.capabilities.isCreativeMode;waterTimer += 20)
			{
				
				/**Reduce the player's water for normal living*/
				waterLevel -= 1+(tempWaterMod/2);
				if(player.isInWater())
				{
					waterLevel = getMaxWater(player);
				}
				if(waterLevel < 0)
					waterLevel = 0;
				if(waterLevel == 0 && temp > 30)
					player.attackEntityFrom(DamageSource.generic, 1);
			}
		}
	}

	public int getMaxWater(EntityPlayer player)
	{
		return (int) ((TFC_Time.dayLength*2/10)+(200*player.experienceLevel));
	}

	/**
	 * Get the player's food level.
	 */
	@Override
	public int getFoodLevel()
	{
		return (int) this.foodLevel;
	}

	/**
	 * If foodLevel is not max.
	 */
	@Override
	public boolean needFood()
	{
		return this.foodLevel < 100;
	}
	
	public boolean needFood(int filling)
	{
		return needFood() && this.foodLevel + filling < 140;
	}

	/**
	 * Reads food stats from an NBT object.
	 */
	@Override
	public void readNBT(NBTTagCompound par1NBTTagCompound)
	{
		if (par1NBTTagCompound.hasKey("foodCompound"))
		{
			NBTTagCompound foodCompound = par1NBTTagCompound.getCompoundTag("foodCompound");
			this.waterLevel = foodCompound.getFloat("waterLevel");
			this.foodLevel = foodCompound.getFloat("foodLevel");
			this.foodTimer = foodCompound.getLong("foodTickTimer");
			this.foodSaturationLevel = foodCompound.getFloat("foodSaturationLevel");
			this.foodExhaustionLevel = foodCompound.getFloat("foodExhaustionLevel");
		}
	}

	/**
	 * Writes food stats to an NBT object.
	 */
	@Override
	public void writeNBT(NBTTagCompound par1NBTTagCompound)
	{
		NBTTagCompound foodCompound = new NBTTagCompound();
		foodCompound.setFloat("waterLevel", this.waterLevel);
		foodCompound.setFloat("foodLevel", this.foodLevel);
		foodCompound.setLong("foodTickTimer", this.foodTimer);
		foodCompound.setFloat("foodSaturationLevel", this.foodSaturationLevel);
		foodCompound.setFloat("foodExhaustionLevel", this.foodExhaustionLevel);
		par1NBTTagCompound.setCompoundTag("foodCompound", foodCompound);
	}

	/**
	 * adds input to foodExhaustionLevel to a max of 40
	 */
	@Override
	public void addExhaustion(float par1)
	{
		this.foodExhaustionLevel = Math.min(this.foodExhaustionLevel + par1, 40.0F);
	}

	/**
	 * Get the player's food saturation level.
	 */
	@Override
	public float getSaturationLevel()
	{
		return this.foodSaturationLevel;
	}

	@Override
	public void setFoodLevel(int par1)
	{
		this.foodLevel = par1;
	}
	
	@Override
	public void setFoodSaturationLevel(float par1)
	{
		this.foodSaturationLevel = par1;
	}

	/**
	 * Args: int foodLevel, float foodSaturationModifier
	 */
	@Override
	public void addStats(int par1, float par2)
	{
		this.foodLevel = Math.min(par1 + this.foodLevel, 100);
		this.foodSaturationLevel = Math.min(this.foodSaturationLevel + (float)par1 / 3 * par2 * 2.0F, (float)this.foodLevel);
	}

	/**
	 * Eat some food.
	 */
	@Override
	public void addStats(ItemFood par1ItemFood)
	{
		this.addStats(par1ItemFood.getHealAmount(), par1ItemFood.getSaturationModifier());
	}
	
	public void restoreWater(EntityPlayer player, int w)
	{
		Math.min(this.waterLevel += w, this.getMaxWater(player));
	}
	
	public void resetTimers()
	{
		waterTimer = TFC_Time.getTotalTicks();
		foodTimer = TFC_Time.getTotalTicks();
		foodHealTimer = TFC_Time.getTotalTicks();
	}
}
