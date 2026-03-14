package com.erydevs.gui.menu;

import com.erydevs.gui.Entry;
import java.util.*;

public class MenuCache {
    private final Map<Integer, Entry> combinedSlotMap = new HashMap<>();
    private final Map<String, Map<Integer, Entry>> entriesByTitle = new HashMap<>();
    private final Map<String, Map<Integer, List<String>>> actionsByTitle = new HashMap<>();
    private final Map<String, Integer> exitSlotByTitle = new HashMap<>();
    private final Map<String, Integer> autobuyerSlotByTitle = new HashMap<>();
    private final Map<String, String> menuNameByTitle = new HashMap<>();

    public void addMenu(String title, String menuName, int exitSlot, int autobuyerSlot,
                        Map<Integer, Entry> entries, Map<Integer, List<String>> actions) {
        entriesByTitle.put(title, entries);
        actionsByTitle.put(title, actions);
        exitSlotByTitle.put(title, exitSlot);
        autobuyerSlotByTitle.put(title, autobuyerSlot);
        menuNameByTitle.put(title, menuName);

        for (Map.Entry<Integer, Entry> e : entries.entrySet()) {
            combinedSlotMap.put(combinedSlotMap.size(), e.getValue());
        }
    }

    public boolean isManagedTitle(String title) {
        return entriesByTitle.containsKey(title);
    }

    public Entry getEntry(String title, int slot) {
        Map<Integer, Entry> m = entriesByTitle.get(title);
        return m != null ? m.get(slot) : null;
    }

    public List<String> getActions(String title, int slot) {
        Map<Integer, List<String>> m = actionsByTitle.get(title);
        return m != null ? m.get(slot) : null;
    }

    public int getExitSlot(String title) {
        return exitSlotByTitle.getOrDefault(title, -1);
    }

    public int getAutobuyerSlot(String title) {
        return autobuyerSlotByTitle.getOrDefault(title, -1);
    }

    public String getMenuName(String title) {
        return menuNameByTitle.getOrDefault(title, "menu");
    }

    public Map<Integer, Entry> getSlotMap() {
        return combinedSlotMap;
    }

    public Collection<Entry> getAllEntries() {
        List<Entry> out = new ArrayList<>();
        for (Map<Integer, Entry> m : entriesByTitle.values()) {
            out.addAll(m.values());
        }
        return out;
    }

    public void clear() {
        combinedSlotMap.clear();
        entriesByTitle.clear();
        actionsByTitle.clear();
        exitSlotByTitle.clear();
        autobuyerSlotByTitle.clear();
        menuNameByTitle.clear();
    }
}
