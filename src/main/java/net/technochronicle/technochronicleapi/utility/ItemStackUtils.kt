package net.technochronicle.technochronicleapi.utility

import net.minecraft.world.item.ItemStack

fun ItemStack.isNotEmpty(): Boolean = !this.isEmpty
