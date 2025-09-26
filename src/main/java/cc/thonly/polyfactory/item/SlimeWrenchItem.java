package cc.thonly.polyfactory.item;

import cc.thonly.polyfactory.block.entity.TurntableBlockEntity;
import cc.thonly.polyfactory.component.ModDataComponents;
import cc.thonly.polyfactory.component.Selector;
import cc.thonly.polyfactory.datagen.ModLanguageKey;
import eu.pb4.polyfactory.block.mechanical.TurntableBlock;
import net.fabricmc.fabric.api.event.player.AttackBlockCallback;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.Optional;

public class SlimeWrenchItem extends Item {
    public SlimeWrenchItem(Settings settings) {
        super(settings);
    }

    // 左键：选择第一个点
    static {
        AttackBlockCallback.EVENT.register((player, world, hand, pos, dir) -> {
            ItemStack itemStack = player.getStackInHand(hand);
            if (!(itemStack.getItem() instanceof SlimeWrenchItem)) {
                return ActionResult.PASS;
            }

            if (!world.isClient) {
                Selector selector = itemStack.getOrDefault(
                        ModDataComponents.SELECTOR,
                        new Selector(Optional.empty(), Optional.empty(), world.getRegistryKey())
                );

                selector = new Selector(Optional.of(pos), selector.blockPosB(), world.getRegistryKey());
                itemStack.set(ModDataComponents.SELECTOR, selector);

                player.sendMessage(
                        Text.translatable(ModLanguageKey.ITEM_SELECT_FIRST.toString(), pos.toShortString()),
                        true
                );
                return ActionResult.SUCCESS_SERVER;
            }
            return ActionResult.SUCCESS;
        });
    }

    // 右键：选择第二个点
    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        World world = context.getWorld();
        Hand hand = context.getHand();
        PlayerEntity player = context.getPlayer();

        if (player == null) {
            return ActionResult.PASS;
        }

        ItemStack itemStack = player.getStackInHand(hand);

        if (!world.isClient) {
            BlockPos pos = context.getBlockPos();
            BlockState blockState = world.getBlockState(pos);

            Selector selector = itemStack.getOrDefault(
                    ModDataComponents.SELECTOR,
                    new Selector(Optional.empty(), Optional.empty(), world.getRegistryKey())
            );
            if (player.isSneaking() && selector.copy().isComplete() && blockState.getBlock() instanceof TurntableBlock) {
                int size = 0;
                for (BlockPos next : BlockPos.iterate(selector.toBox())) {
                    size++;
                }
                if (size > 32 * 32 * 32 && !player.isInCreativeMode()) {
                    player.sendMessage(Text.translatable(ModLanguageKey.ITEM_SELECT_FAIL.getCode()), true);
                    return ActionResult.SUCCESS_SERVER;
                }
                itemStack.remove(ModDataComponents.SELECTOR);
                BlockEntity blockEntity = world.getBlockEntity(pos);
                if (blockEntity instanceof TurntableBlockEntity turntableBlockEntity) {
                    boolean update = turntableBlockEntity.update((ServerWorld) world, selector, Action.SET);
//                    System.out.println(update);
                }
                player.sendMessage(Text.translatable(ModLanguageKey.ITEM_SELECT_SUCCESS.getCode()), true);
                return ActionResult.SUCCESS_SERVER;
            }

            selector = new Selector(selector.blockPosA(), Optional.of(pos), world.getRegistryKey());
            itemStack.set(ModDataComponents.SELECTOR, selector);

            player.sendMessage(
                    Text.translatable(ModLanguageKey.ITEM_SELECT_SECOND.toString(), pos.toShortString()),
                    true
            );

            if (selector.isComplete()) {
                player.sendMessage(
                        Text.translatable(ModLanguageKey.ITEM_SELECT_FINISH.toString()),
                        false
                );
            }

            return ActionResult.SUCCESS_SERVER;
        }

        return super.useOnBlock(context);
    }

    @Override
    public boolean canMine(ItemStack stack, BlockState state, World world, BlockPos pos, LivingEntity user) {
        return false;
    }

    public enum Action {
        SET(),
        REMOVE(),
    }
}
