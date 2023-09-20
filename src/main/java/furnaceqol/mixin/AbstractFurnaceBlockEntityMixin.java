package furnaceqol.mixin;

import furnaceqol.FurnaceQOL;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.Recipe;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.apache.logging.log4j.core.jmx.Server;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import net.minecraft.entity.ItemEntity;

@Mixin(AbstractFurnaceBlockEntity.class)
public class AbstractFurnaceBlockEntityMixin {
	@Inject(at = @At("TAIL"), method = "tick")
	private static void init(World world, BlockPos pos, BlockState state, AbstractFurnaceBlockEntity furnace, CallbackInfo ci) {
		if (world.isClient()) {
			return;
		}

		ItemStack input = (ItemStack) furnace.getStack(0);
		ItemStack output = (ItemStack) furnace.getStack(2);

		if (!willItemSmelt(input, furnace, world) && !input.isEmpty()) {
			world.spawnEntity(new ItemEntity(world, pos.getX(), pos.getY(), pos.getZ(), output));
			furnace.removeStack(2);
			FurnaceQOL.LOGGER.info("create item");
		}
	}

	@Unique
	private static boolean willItemSmelt(ItemStack input, AbstractFurnaceBlockEntity furnace, World world) {
		Recipe recipe;
		if (!input.isEmpty()) {
			recipe = ((AbstractFurnaceBlockEntityAccessors)furnace).getMatchGetter().getFirstMatch(furnace, world).orElse(null);
		} else {
			recipe = null;
		}
		boolean canAccept = ((AbstractFurnaceBlockEntityAccessors) furnace).invokeCanAcceptRecipeOutput(
				world.getRegistryManager(),
				recipe,
				((AbstractFurnaceBlockEntityAccessors) furnace).getInventory(),
				furnace.getMaxCountPerStack()
		);
		return canAccept && !input.isEmpty();
	}
}


