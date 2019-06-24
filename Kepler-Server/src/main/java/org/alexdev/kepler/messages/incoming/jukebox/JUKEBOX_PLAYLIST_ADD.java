package org.alexdev.kepler.messages.incoming.jukebox;

import org.alexdev.kepler.dao.mysql.SongMachineDao;
import org.alexdev.kepler.game.item.base.ItemBehaviour;
import org.alexdev.kepler.game.fuserights.Fuseright;
import org.alexdev.kepler.game.player.Player;
import org.alexdev.kepler.game.room.Room;
import org.alexdev.kepler.game.song.SongPlaylist;
import org.alexdev.kepler.messages.outgoing.songs.SONG_PLAYLIST;
import org.alexdev.kepler.messages.types.MessageEvent;
import org.alexdev.kepler.server.netty.streams.NettyRequest;

public class JUKEBOX_PLAYLIST_ADD implements MessageEvent {
    @Override
    public void handle(Player player, NettyRequest reader) throws Exception {
        if (player.getRoomUser().getRoom() == null) {
            return;
        }

        Room room = player.getRoomUser().getRoom();

        if (room.getItemManager().getSoundMachine() == null || !room.getItemManager().getSoundMachine().hasBehaviour(ItemBehaviour.JUKEBOX)) {
            return;
        }

        if (!room.hasRights(player.getDetails().getId()) && !player.hasFuse(Fuseright.ANY_ROOM_CONTROLLER)) {
            //return;
        }

        int songId = reader.readInt();

        SongMachineDao.removePlaylistSong(songId, room.getItemManager().getSoundMachine().getId());

        var playList = SongMachineDao.getSongPlaylist(room.getItemManager().getSoundMachine().getId());
        var loadedDiscs = SongMachineDao.getTracks(room.getItemManager().getSoundMachine().getId());

        // Don't load a song if it's not in the jukebox
        if (loadedDiscs.values().stream().noneMatch(disc -> disc == songId)) {
            return;
        }

        int newSlotId = playList.size() + 1;

        SongMachineDao.addPlaylist(room.getItemManager().getSoundMachine().getId(), songId, newSlotId);
        playList.add(new SongPlaylist(room.getItemManager().getSoundMachine().getId(), SongMachineDao.getSong(songId), newSlotId));

        room.send(new SONG_PLAYLIST(playList));
    }
}
