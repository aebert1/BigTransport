package com.bigeebs.bigtransport.creativeTab;


import com.bigeebs.bigtransport.reference.Reference;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;

/**
 * Created by bigee_000 on 7/12/2015.
 */
public class CreativeTab {

    public static final CreativeTabs BIG_TRANSPORT = new CreativeTabs(Reference.MOD_ID.toLowerCase()) {

        @Override
        public Item getTabIconItem() {
            return null;
        }
    };
}
