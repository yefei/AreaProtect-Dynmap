package cn.minecon.areaprotect.dynmap;

import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.OfflinePlayer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.server.PluginEnableEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.dynmap.DynmapAPI;
import org.dynmap.markers.AreaMarker;
import org.dynmap.markers.MarkerAPI;
import org.dynmap.markers.MarkerSet;

import cn.minecon.areaprotect.AreaProtect;
import cn.minecon.areaprotect.Config;
import cn.minecon.areaprotect.Flag;
import cn.minecon.areaprotect.area.Area;
import cn.minecon.areaprotect.events.AreaCreatedEvent;
import cn.minecon.areaprotect.events.AreaDeletedEvent;
import cn.minecon.areaprotect.events.AreaFlagsChangedEvent;
import cn.minecon.areaprotect.events.AreaOwnerChangedEvent;

public class AreaDynmap extends JavaPlugin {
	private static Logger logger;
	private Plugin dynmap;
	private DynmapAPI dynmapAPI;
	private AreaProtect areaProtect;
	private MarkerAPI markerAPI;
	private MarkerSet markerSet;
	
	@Override
	public void onLoad() {
		logger = getLogger();
	}
	
	@Override
    public void onEnable() {
		PluginManager pm = getServer().getPluginManager();
		
		dynmap = pm.getPlugin("dynmap");
        if(dynmap == null) {
            logger.log(Level.SEVERE, "Cannot find dynmap!");
            return;
        }
        dynmapAPI = (DynmapAPI) dynmap;
        
        // Get AreaProtect
        Plugin p = pm.getPlugin("AreaProtect");
        if(p == null) {
        	logger.log(Level.SEVERE, "Cannot find AreaProtect!");
            return;
        }
        areaProtect = (AreaProtect) p;
        
        pm.registerEvents(new ServerListener(), this);
        
        if(dynmap.isEnabled() && areaProtect.isEnabled()) {
        	activate();
        }
	}
	
	public class ServerListener implements Listener {
		@EventHandler(priority=EventPriority.MONITOR)
	    public void onPluginEnable(PluginEnableEvent event) {
	        Plugin p = event.getPlugin();
	        String name = p.getDescription().getName();
	        if(name.equals("dynmap") || name.equals("AreaProtect")) {
	            if(dynmap.isEnabled() && areaProtect.isEnabled()) {
	            	activate();
	            }
	        }
	    }
		
		@EventHandler(priority=EventPriority.MONITOR)
		public void onAreaCreated(AreaCreatedEvent event) {
			createAreaMarker(event.getArea());
		}
		
		@EventHandler(priority=EventPriority.MONITOR)
		public void onAreaDeleted(AreaDeletedEvent event) {
			deleteAreaMarker(event.getArea());
		}
		
		@EventHandler(priority=EventPriority.MONITOR)
		public void onAreaFlagsChanged(AreaFlagsChangedEvent event) {
			updateAreaDescription(event.getArea());
		}
		
		@EventHandler(priority=EventPriority.MONITOR)
		public void onAreaOwnerChanged(AreaOwnerChangedEvent event) {
			updateAreaDescription(event.getArea());
		}
	}
	
	public void activate() {
		/* Now, get markers API */
        markerAPI = dynmapAPI.getMarkerAPI();
        if(markerAPI == null) {
        	logger.log(Level.SEVERE, "Error loading dynmap marker API!");
            return;
        }
        
        markerSet = markerAPI.getMarkerSet("areaprotect.markerset");
        if(markerSet == null) {
        	markerSet = markerAPI.createMarkerSet("areaprotect.markerset", "AreaProtect", null, false);
        } else {
        	markerSet.setMarkerSetLabel("AreaProtect");
        }
        if(markerSet == null) {
        	logger.log(Level.SEVERE, "Error creating marker set");
            return;
        }
        
        markerSet.setLayerPriority(10);
        markerSet.setHideByDefault(false);
        
        for (Area area : areaProtect.getAreaManager().getAll()) {
        	createAreaMarker(area);
        }
	}
	
	private Map<String, AreaMarker> resareas = new HashMap<String, AreaMarker>();
	
	public void createAreaMarker(Area area) {
		final double[] x = new double[4];
		final double[] z = new double[4];
        
        x[0] = area.getLowX(); z[0] = area.getLowZ();
        x[1] = area.getLowX(); z[1] = area.getHighZ()+1.0;
        x[2] = area.getHighX() + 1.0; z[2] = area.getHighZ()+1.0;
        x[3] = area.getHighX() + 1.0; z[3] = area.getLowZ();
        
        final String id = area.getUUID().toString();
        final String name = area.getName();
        
        AreaMarker m = resareas.get(id); /* Existing area? */
        if (m == null) {
            m = markerSet.createAreaMarker(id, name, false, area.getWorld().getName(), x, z, false);
            if(m == null) return;
            m.setLineStyle(2, 0.5, 0x00FF00);
            m.setFillStyle(0.05, 0x00FF00);
            resareas.put(id, m);
        } else {
            m.setCornerLocations(x, z); /* Replace corner locations */
            m.setLabel(name);   /* Update label */
        }
        updateAreaDescription(area);
	}
	
	public void updateAreaDescription(Area area) {
		AreaMarker m = resareas.get(area.getUUID().toString());
		if (m != null) {
			StringBuilder desc = new StringBuilder();
			
			desc.append(getMessage("Info.Name", area.getName()));
			desc.append("<br>");
	        desc.append(getMessage("Info.Owner", area.getOwnerName()));
	        desc.append("<br>");
	        desc.append(getMessage("Info.Size", area.getSize(), area.getXSize(), area.getZSize()));
	        desc.append("<br>");
	        desc.append(getMessage("Info.CreationDate", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(area.getCreationDate()))));
	        desc.append("<br>");
	        desc.append(getMessage("Info.AreaFlags", getFlagsString(area.getAreaFlags())));
	        desc.append("<br>");
        	desc.append(getMessage("Info.PlayerFlags"));
	        if (area.getPlayerFlags().size() > 0) {
	        	desc.append("<ul style=\"margin:0;padding-left:18px;\">");
				Map<UUID, Map<Flag, Boolean>> playerFlags = area.getPlayerFlags();
				for (UUID uuid : playerFlags.keySet()) {
					Map<Flag, Boolean> flags = playerFlags.get(uuid);
					if (flags == null || flags.size() == 0) continue;
					final OfflinePlayer p = getServer().getOfflinePlayer(uuid);
					desc.append("<li>");
					desc.append((p == null ? uuid.toString() : p.getName()) + ": " + getFlagsString(flags));
					desc.append("</li>");
				}
				desc.append("</ul>");
			} else {
				desc.append(getMessage("NotFlags"));
			}
	        
	        m.setDescription(desc.toString());
		}
	}
	
	public void deleteAreaMarker(Area area) {
		AreaMarker m = resareas.remove(area.getUUID().toString());
		if (m != null) {
			m.deleteMarker();
		}
	}
	
	public String getFlagsString(Map<Flag, Boolean> flags) {
		if (flags == null) {
			return null;
		}
		StringBuilder out = new StringBuilder();
		for (Entry<Flag, Boolean> flag : flags.entrySet()) {
			if (flag.getValue()) {
				out.append("<GREEN>+");
			} else {
				out.append("<RED>-");
			}
			out.append(flag.getKey().getName());
			out.append(" ");
		}
		return out.length() == 0 ? getMessage("NotFlags") : MessageColor.htmlColors(out.toString());
	}
	
	public static String getMessage(final String key, final Object... args) {
		final String path = "Messages." + key;
		String message = Config.getConfig().getString(path);
		
		if (message == null) {
			message = "<DARK_GRAY>** Miss Messages: " + path;
		}
		
		if (args != null) {
            MessageFormat formatter = new MessageFormat(message);
            message = formatter.format(args);
        }
		
		return MessageColor.htmlColors(message);
	}
}
