package com.bigeebs.bigtransport.block;

import com.bigeebs.bigtransport.creativeTab.CreativeTab;
import com.bigeebs.bigtransport.reference.Reference;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;

public class BaseBlock extends Block {

    public BaseBlock(Material material) {
        super(material);
        this.setCreativeTab(CreativeTab.BIG_TRANSPORT);
    }

    public BaseBlock()
    {
        super(Material.rock);
        this.setCreativeTab(CreativeTab.BIG_TRANSPORT);
    }

    @Override
    public String getUnlocalizedName()
    {
        return String.format("tile.%s:%s", Reference.MOD_ID.toLowerCase(), getUnwrappedUnlocalizedName(super.getUnlocalizedName()));
    }

    public String getUnwrappedUnlocalizedName(String unlocalizedName) {

        return unlocalizedName.substring(unlocalizedName.indexOf(".") + 1);
    }
}
