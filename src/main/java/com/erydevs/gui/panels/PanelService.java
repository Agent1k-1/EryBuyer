package com.erydevs.gui.panels;

import com.erydevs.EryBuyer;
import com.erydevs.gui.impl.head.MaterialHeadParser;
import com.erydevs.gui.impl.head.ParsedMaterial;
import com.erydevs.gui.impl.head.SkullUtils;
import com.erydevs.utils.HexUtils;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class PanelService {

    private final EryBuyer plugin;

    public PanelService(EryBuyer plugin) {
        this.plugin = plugin;
    }

    public void addPanelsToInventory(Inventory inv, FileConfiguration cfg, int size) {
        List<PanelConfig> panels = loadPanels(cfg);
        for (PanelConfig panel : panels) {
            addPanel(inv, panel, size);
        }
    }
    
    private List<PanelConfig> loadPanels(FileConfiguration cfg) {
        List<PanelConfig> panels = new ArrayList<>();
        
        if (!cfg.isList("panels")) {
            return panels;
        }
        
        List<?> panelsList = cfg.getList("panels");
        if (panelsList == null) return panels;
        
        for (Object obj : panelsList) {
            if (obj instanceof Map) {
                Map<String, Object> panelMap = (Map<String, Object>) obj;
                PanelConfig panel = parsePanelFromMap(panelMap);
                if (panel != null) {
                    panels.add(panel);
                }
            }
        }
        
        return panels;
    }
    
    private PanelConfig parsePanelFromMap(Map<String, Object> map) {
        String material = (String) map.get("material");
        String name = (String) map.get("name");
        List<?> lore = (List<?>) map.get("lore");
        List<?> slots = (List<?>) map.get("slots");
        
        if (material == null || slots == null) {
            return null;
        }

        ParsedMaterial pm = MaterialHeadParser.parse(material);
        if (pm == null) {
            return null;
        }
        
        List<String> loreList = new ArrayList<>();
        if (lore != null) {
            for (Object line : lore) {
                loreList.add(line.toString());
            }
        }
        
        List<Integer> slotList = new ArrayList<>();
        for (Object slot : slots) {
            slotList.add(((Number) slot).intValue());
        }
        
        return new PanelConfig(pm.getMaterial(), pm.getHeadTextureBase64(), name, loreList, slotList);
    }
    
    private void addPanel(Inventory inv, PanelConfig panel, int size) {
        ItemStack item = createPanelItem(panel);
        
        for (int slot : panel.getSlots()) {
            if (slot < size) {
                inv.setItem(slot, item.clone());
            }
        }
    }
    
    private ItemStack createPanelItem(PanelConfig panel) {
        ItemStack item;
        String headTex = panel.getHeadTextureBase64();
        if (headTex != null && !headTex.isEmpty()) {
            item = SkullUtils.getSkullByBase64(plugin, headTex);
        } else {
            item = new ItemStack(panel.getMaterial());
        }
        ItemMeta meta = item.getItemMeta();
        
        if (meta != null) {
            if (panel.getName() != null) {
                meta.setDisplayName(HexUtils.colorize(panel.getName()));
            }
            
            List<String> lore = new ArrayList<>();
            for (String line : panel.getLore()) {
                String colorized = line.trim().isEmpty() ? " " : HexUtils.colorize(line);
                lore.add(colorized);
            }
            if (lore.isEmpty()) lore.add(" ");
            
            meta.setLore(lore);
            item.setItemMeta(meta);
        }
        
        return item;
    }
}
