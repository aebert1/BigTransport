package net.minecraft.world.storage;

import com.google.common.collect.Lists;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.List;
import net.minecraft.client.AnvilConverterException;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.IProgressUpdate;
import net.minecraft.util.datafix.DataFixer;
import net.minecraft.util.datafix.FixTypes;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class SaveFormatOld implements ISaveFormat
{
    private static final Logger logger = LogManager.getLogger();
    /** Reference to the File object representing the directory for the world saves */
    public final File savesDirectory;
    protected final DataFixer dataFixer;

    public SaveFormatOld(File savesDirectoryIn, DataFixer dataFixerIn)
    {
        this.dataFixer = dataFixerIn;

        if (!savesDirectoryIn.exists())
        {
            savesDirectoryIn.mkdirs();
        }

        this.savesDirectory = savesDirectoryIn;
    }

    /**
     * Returns the name of the save format.
     */
    @SideOnly(Side.CLIENT)
    public String getName()
    {
        return "Old Format";
    }

    @SideOnly(Side.CLIENT)
    public List<SaveFormatComparator> getSaveList() throws AnvilConverterException
    {
        List<SaveFormatComparator> list = Lists.<SaveFormatComparator>newArrayList();

        for (int i = 0; i < 5; ++i)
        {
            String s = "World" + (i + 1);
            WorldInfo worldinfo = this.getWorldInfo(s);

            if (worldinfo != null)
            {
                list.add(new SaveFormatComparator(worldinfo, s, "", worldinfo.getSizeOnDisk(), false));
            }
        }

        return list;
    }

    @SideOnly(Side.CLIENT)
    public void flushCache()
    {
    }

    /**
     * Returns the world's WorldInfo object
     */
    public WorldInfo getWorldInfo(String saveName)
    {
        File file1 = new File(this.savesDirectory, saveName);

        if (!file1.exists())
        {
            return null;
        }
        else
        {
            File file2 = new File(file1, "level.dat");

            if (file2.exists())
            {
                WorldInfo worldinfo = func_186353_a(file2, this.dataFixer);

                if (worldinfo != null)
                {
                    return worldinfo;
                }
            }

            file2 = new File(file1, "level.dat_old");
            return file2.exists() ? func_186353_a(file2, this.dataFixer) : null;
        }
    }

    public static WorldInfo func_186353_a(File p_186353_0_, DataFixer dataFixerIn)
    {
        try
        {
            NBTTagCompound nbttagcompound = CompressedStreamTools.readCompressed(new FileInputStream(p_186353_0_));
            NBTTagCompound nbttagcompound1 = nbttagcompound.getCompoundTag("Data");
            return new WorldInfo(dataFixerIn.process(FixTypes.LEVEL, nbttagcompound1));
        }
        catch (Exception exception)
        {
            logger.error((String)("Exception reading " + p_186353_0_), (Throwable)exception);
            return null;
        }
    }

    //Forge: Special version of the above that runs during actual world loading and not metadata gathering.
    public static WorldInfo loadAndFix(File file, DataFixer fixer, SaveHandler save)
    {
        try
        {
            NBTTagCompound nbt = CompressedStreamTools.readCompressed(new FileInputStream(file));
            WorldInfo info = new WorldInfo(fixer.process(FixTypes.LEVEL, nbt.getCompoundTag("Data")));
            net.minecraftforge.fml.common.FMLCommonHandler.instance().handleWorldDataLoad(save, info, nbt);
            return info;
        }
        catch (net.minecraftforge.fml.common.StartupQuery.AbortedException e) { throw e; }
        catch (Exception exception)
        {
            logger.error((String)("Exception reading " + file), (Throwable)exception);
            return null;
        }
    }

    /**
     * Renames the world by storing the new name in level.dat. It does *not* rename the directory containing the world
     * data.
     */
    @SideOnly(Side.CLIENT)
    public void renameWorld(String dirName, String newName)
    {
        File file1 = new File(this.savesDirectory, dirName);

        if (file1.exists())
        {
            File file2 = new File(file1, "level.dat");

            if (file2.exists())
            {
                try
                {
                    NBTTagCompound nbttagcompound = CompressedStreamTools.readCompressed(new FileInputStream(file2));
                    NBTTagCompound nbttagcompound1 = nbttagcompound.getCompoundTag("Data");
                    nbttagcompound1.setString("LevelName", newName);
                    CompressedStreamTools.writeCompressed(nbttagcompound, new FileOutputStream(file2));
                }
                catch (Exception exception)
                {
                    exception.printStackTrace();
                }
            }
        }
    }

    /**
     * Returns back a loader for the specified save directory
     */
    public ISaveHandler getSaveLoader(String saveName, boolean storePlayerdata)
    {
        return new SaveHandler(this.savesDirectory, saveName, storePlayerdata, this.dataFixer);
    }

    @SideOnly(Side.CLIENT)
    public boolean isNewLevelIdAcceptable(String saveName)
    {
        File file1 = new File(this.savesDirectory, saveName);

        if (file1.exists())
        {
            return false;
        }
        else
        {
            try
            {
                file1.mkdir();
                file1.delete();
                return true;
            }
            catch (Throwable throwable)
            {
                logger.warn("Couldn\'t make new level", throwable);
                return false;
            }
        }
    }

    /**
     * @args: Takes one argument - the name of the directory of the world to delete. @desc: Delete the world by deleting
     * the associated directory recursively.
     */
    @SideOnly(Side.CLIENT)
    public boolean deleteWorldDirectory(String saveName)
    {
        File file1 = new File(this.savesDirectory, saveName);

        if (!file1.exists())
        {
            return true;
        }
        else
        {
            logger.info("Deleting level " + saveName);

            for (int i = 1; i <= 5; ++i)
            {
                logger.info("Attempt " + i + "...");

                if (deleteFiles(file1.listFiles()))
                {
                    break;
                }

                logger.warn("Unsuccessful in deleting contents.");

                if (i < 5)
                {
                    try
                    {
                        Thread.sleep(500L);
                    }
                    catch (InterruptedException var5)
                    {
                        ;
                    }
                }
            }

            return file1.delete();
        }
    }

    /**
     * @args: Takes one argument - the list of files and directories to delete. @desc: Deletes the files and directory
     * listed in the list recursively.
     */
    @SideOnly(Side.CLIENT)
    protected static boolean deleteFiles(File[] files)
    {
        for (int i = 0; i < files.length; ++i)
        {
            File file1 = files[i];
            logger.debug("Deleting " + file1);

            if (file1.isDirectory() && !deleteFiles(file1.listFiles()))
            {
                logger.warn("Couldn\'t delete directory " + file1);
                return false;
            }

            if (!file1.delete())
            {
                logger.warn("Couldn\'t delete file " + file1);
                return false;
            }
        }

        return true;
    }

    @SideOnly(Side.CLIENT)
    public boolean isConvertible(String saveName)
    {
        return false;
    }

    /**
     * gets if the map is old chunk saving (true) or McRegion (false)
     */
    public boolean isOldMapFormat(String saveName)
    {
        return false;
    }

    /**
     * converts the map to mcRegion
     */
    public boolean convertMapFormat(String filename, IProgressUpdate progressCallback)
    {
        return false;
    }

    /**
     * Return whether the given world can be loaded.
     */
    @SideOnly(Side.CLIENT)
    public boolean canLoadWorld(String saveName)
    {
        File file1 = new File(this.savesDirectory, saveName);
        return file1.isDirectory();
    }

    public File getFile(String p_186352_1_, String p_186352_2_)
    {
        return new File(new File(this.savesDirectory, p_186352_1_), p_186352_2_);
    }
}