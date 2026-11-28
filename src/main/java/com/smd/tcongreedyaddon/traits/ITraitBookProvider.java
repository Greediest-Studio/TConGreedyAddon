package com.smd.tcongreedyaddon.traits;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import slimeknights.tconstruct.library.book.content.ContentModifier;

public interface ITraitBookProvider {
    @SideOnly(Side.CLIENT)
    ContentModifier getBookContent();
}
