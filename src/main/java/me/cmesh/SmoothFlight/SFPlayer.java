package me.cmesh.SmoothFlight;

import java.util.Random;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

public class SFPlayer {
	private static SmoothFlight plugin;

	private UUID playerUUID;
	private Long lastFly;
	private boolean hover;

	public SFPlayer(Player player, SmoothFlight instance) {
		this.playerUUID = player.getUniqueId();
		plugin = instance;
	}

	public Material getTool() {
		return getPlayer(playerUUID).getItemInHand().getType();
	}

	private boolean hasPermission(String permission) {
		Player player = getPlayer(playerUUID);
		return player.isOp() || player.hasPermission(permission);
	}

	public boolean canFly() {
		Player player = getPlayer(playerUUID);

		boolean correctTool = plugin.flyTool == player.getItemInHand().getType();
		boolean hasFood = player.getFoodLevel() > 0;
		Material block = player.getLocation().getBlock().getType();
		boolean aboveWater = block != Material.WATER && block != Material.STATIONARY_WATER;
		boolean properHeight = hasPermission("smoothfly.ignoreHeight")
				|| (player.getLocation().getY() > plugin.minHeight && player.getLocation().getY() < plugin.maxHeight);
		boolean world = plugin.worlds.contains(player.getWorld().getName());
		boolean dreamWorld = plugin.dreamWorlds.contains(player.getWorld().getName());
		return (dreamWorld || hasPermission("smoothflight.fly") && properHeight) && world && correctTool && hasFood
				&& aboveWater;
	}
	public void fly() {
		Player player = getPlayer(playerUUID);
		if (plugin.smoke)
			SmokeUtil.spawnCloudRandom(player.getLocation(), 4);

		double speed = player.isSneaking() ? plugin.flySpeedSneak : plugin.flySpeed;

		Vector dir = player.getLocation().getDirection();
		dir = dir.multiply(speed);
		dir.setY(dir.getY() + 0.60 * speed);
		player.setVelocity(dir);

		player.setFallDistance(0);

		hunger(plugin.hunger);

		lastFly = player.getWorld().getTime();
	}

	public void toggleHover() {
		if (hover)
			hover = false;
		else if (canFly())
			hover = true;
	}

	public boolean isHovering() {
		return hover;
	}

	public void hover() {
		Player player = getPlayer(playerUUID);
		if (player.getFoodLevel() <= 1 || player.getItemInHand().getType() != plugin.flyTool)
			hover = false;

		if (plugin.smoke)
			SmokeUtil.spawnCloudRandom(player.getLocation(), 1);

		player.setVelocity(new Vector(0, 0.1D, 0));
		player.setFallDistance(0);

		hunger(plugin.hunger / 2);

		lastFly = player.getWorld().getTime();
	}

	private void hunger(int hunger) {
		Player player = getPlayer(playerUUID);
		if ((!player.isOp() || plugin.opHunger) && !hasPermission("smoothflight.nohunger")
				&& new Random().nextInt(100) < hunger)
			player.setFoodLevel(player.getFoodLevel() - 1);
	}

	public boolean isFlying() {
		return lastFly != null && lastFly >= (getPlayer(playerUUID).getWorld().getTime() - 100);
	}

	private Player getPlayer(UUID id) {
		return Bukkit.getPlayer(id);
	}
}