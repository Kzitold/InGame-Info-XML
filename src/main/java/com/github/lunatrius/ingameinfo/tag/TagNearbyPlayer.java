package com.github.lunatrius.ingameinfo.tag;

import com.github.lunatrius.ingameinfo.client.gui.overlay.InfoIcon;
import com.github.lunatrius.ingameinfo.tag.registry.TagRegistry;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.entity.player.EntityPlayer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public abstract class TagNearbyPlayer extends Tag {
    public static final int MAXIMUM_INDEX = 16;

    private static final Comparator<EntityPlayer> PLAYER_DISTANCE_COMPARATOR = new Comparator<EntityPlayer>() {
        @Override
        public int compare(final EntityPlayer playerA, final EntityPlayer playerB) {
            if (Tag.player == null) {
                return 0;
            }

            final double distanceA = Tag.player.getDistanceSqToEntity(playerA);
            final double distanceB = Tag.player.getDistanceSqToEntity(playerB);
            if (distanceA > distanceB) {
                return 1;
            } else if (distanceA < distanceB) {
                return -1;
            }
            return 0;
        }
    };
    protected static EntityPlayer[] nearbyPlayers = null;
    protected final int index;

    public TagNearbyPlayer(final int index) {
        this.index = index;
    }

    @Override
    public String getName() {
        return super.getName() + this.index;
    }

    @Override
    public String[] getAliases() {
        final String[] aliases = super.getAliases();
        final String[] aliasesIndexed = new String[aliases.length];
        for (int i = 0; i < aliases.length; i++) {
            aliasesIndexed[i] = aliases[i] + this.index;
        }
        return aliasesIndexed;
    }

    @Override
    public boolean isIndexed() {
        return true;
    }

    @Override
    public int getMaximumIndex() {
        return MAXIMUM_INDEX - 1;
    }

    @Override
    public String getCategory() {
        return "nearbyplayer";
    }

    protected static void updateNearbyPlayers() {
        if (nearbyPlayers == null) {
            final List<EntityPlayer> playerList = new ArrayList<EntityPlayer>();
            for (final EntityPlayer player : world.playerEntities) {
                if (player != Tag.player && !player.isSneaking()) {
                    playerList.add(player);
                }
            }

            Collections.sort(playerList, PLAYER_DISTANCE_COMPARATOR);
            nearbyPlayers = playerList.toArray(new EntityPlayer[playerList.size()]);
        }
    }

    public static class Name extends TagNearbyPlayer {
        public Name(final int index) {
            super(index);
        }

        @Override
        public String getValue() {
            updateNearbyPlayers();
            if (nearbyPlayers.length > this.index) {
                return nearbyPlayers[this.index].getDisplayName().getFormattedText();
            }
            return "";
        }
    }

    public static class Distance extends TagNearbyPlayer {
        public Distance(final int index) {
            super(index);
        }

        @Override
        public String getValue() {
            updateNearbyPlayers();
            if (nearbyPlayers.length > this.index) {
                return String.format(Locale.ENGLISH, "%.2f", nearbyPlayers[this.index].getDistanceToEntity(player));
            }
            return "-1";
        }
    }

    public static class Icon extends TagNearbyPlayer {
        public Icon(final int index) {
            super(index);
        }

        @Override
        public String getValue() {
            updateNearbyPlayers();
            if (nearbyPlayers.length > this.index) {
                final NetworkPlayerInfo playerInfo = minecraft.getConnection().getPlayerInfo(nearbyPlayers[this.index].getUniqueID());
                final InfoIcon icon = new InfoIcon(playerInfo.getLocationSkin());
                icon.setTextureData(8, 8, 8, 8, 64, 64);
                icon.setDisplayDimensions(0, 0, 8, 8);
                info.add(icon);
                return getIconTag(icon);
            }
            return "";
        }
    }

    public static void register() {
        for (int i = 0; i < MAXIMUM_INDEX; i++) {
            TagRegistry.INSTANCE.register(new Name(i).setName("nearbyplayername"));
            TagRegistry.INSTANCE.register(new Distance(i).setName("nearbyplayerdistance"));
            TagRegistry.INSTANCE.register(new Icon(i).setName("nearbyplayericon"));
        }
    }

    public static void releaseResources() {
        nearbyPlayers = null;
    }
}
