package net.technochronicle.technochronicleapi.utils

import net.minecraft.world.item.ItemStack

fun ItemStack.isNotEmpty(): Boolean = !this.isEmpty
