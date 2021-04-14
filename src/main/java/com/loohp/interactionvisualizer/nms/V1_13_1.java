package com.loohp.interactionvisualizer.nms;

import java.util.List;
import java.util.stream.Collectors;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_13_R2.CraftChunk;
import org.bukkit.craftbukkit.v1_13_R2.CraftWorld;
import org.bukkit.craftbukkit.v1_13_R2.util.CraftMagicNumbers;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.EnumWrappers.ItemSlot;
import com.loohp.interactionvisualizer.InteractionVisualizer;
import com.loohp.interactionvisualizer.objectholders.BlockPosition;
import com.loohp.interactionvisualizer.objectholders.BoundingBox;
import com.loohp.interactionvisualizer.objectholders.ChunkPosition;
import com.loohp.interactionvisualizer.objectholders.NMSTileEntitySet;
import com.loohp.interactionvisualizer.objectholders.TileEntity;
import com.loohp.interactionvisualizer.objectholders.TileEntity.TileEntityType;
import com.loohp.interactionvisualizer.objectholders.ValuePairs;

import net.minecraft.server.v1_13_R2.VoxelShape;
import net.minecraft.server.v1_13_R2.WorldServer;

public class V1_13_1 extends NMS {
	
	public List<BoundingBox> getBoundingBoxes(BlockPosition pos) {
		net.minecraft.server.v1_13_R2.BlockPosition blockpos = new net.minecraft.server.v1_13_R2.BlockPosition(pos.getX(), pos.getY(), pos.getZ());
		WorldServer world = ((CraftWorld) pos.getWorld()).getHandle();
		VoxelShape shape = world.getType(blockpos).getShape(world, blockpos);
		return shape.d().stream().map(each -> new BoundingBox(each.minX + pos.getX(), each.minY + pos.getY(), each.minZ + pos.getZ(), each.maxX + pos.getX(), each.maxY + pos.getY(), each.maxZ + pos.getZ())).collect(Collectors.toList());
	}
	
	@Override
	public NMSTileEntitySet<?, ?> getTileEntities(ChunkPosition chunk, boolean load) {
		if (!chunk.isLoaded() && !load) {
			return null;
		}
		World world = chunk.getWorld();
		return new NMSTileEntitySet<net.minecraft.server.v1_13_R2.BlockPosition, net.minecraft.server.v1_13_R2.TileEntity>(((CraftChunk) chunk.getChunk()).getHandle().tileEntities, entry -> {
			net.minecraft.server.v1_13_R2.BlockPosition pos = entry.getKey();
			Material type = CraftMagicNumbers.getMaterial(entry.getValue().getBlock().getBlock());
			TileEntityType tileEntityType = TileEntity.getTileEntityType(type);
			if (tileEntityType != null) {
				return new TileEntity(world, pos.getX(), pos.getY(), pos.getZ(), tileEntityType);
			} else {
				return null;
			}
		});
	}
	
	@Override
	public PacketContainer[] createEntityEquipmentPacket(int entityId, List<ValuePairs<EquipmentSlot, ItemStack>> equipments) {
		PacketContainer[] packets = new PacketContainer[equipments.size()];
		for (int i = 0; i < equipments.size(); i++) {
			ValuePairs<EquipmentSlot, ItemStack> pair = equipments.get(i);
			PacketContainer packet = InteractionVisualizer.protocolManager.createPacket(PacketType.Play.Server.ENTITY_EQUIPMENT);
			packet.getIntegers().write(0, entityId);
			ItemSlot libSlot;
			switch (pair.getFirst()) {
			case CHEST:
				libSlot = ItemSlot.CHEST;
				break;
			case FEET:
				libSlot = ItemSlot.FEET;
				break;
			case HEAD:
				libSlot = ItemSlot.HEAD;
				break;
			case LEGS:
				libSlot = ItemSlot.LEGS;
				break;
			case OFF_HAND:
				libSlot = ItemSlot.OFFHAND;
				break;
			case HAND:
			default:
				libSlot = ItemSlot.MAINHAND;
				break;
			}
			packet.getItemSlots().write(0, libSlot);
			packet.getItemModifier().write(0, pair.getSecond());
			packets[i] = packet;
		}
		return packets;
	}

}
