package bms.player.beatoraja.arena.client;

import bms.player.beatoraja.MainLoader;
import bms.player.beatoraja.arena.enums.ClientToServer;
import bms.player.beatoraja.arena.enums.ServerToClient;
import bms.player.beatoraja.arena.lobby.Lobby;
import bms.player.beatoraja.arena.network.Address;
import bms.player.beatoraja.arena.network.Message;
import bms.player.beatoraja.arena.network.ScoreMessage;
import bms.player.beatoraja.arena.network.SelectedBMSMessage;
import bms.player.beatoraja.modmenu.ImGuiNotify;
import bms.player.beatoraja.song.SongData;
import bms.player.beatoraja.song.SongDatabaseAccessor;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft;
import org.java_websocket.handshake.ServerHandshake;
import org.msgpack.core.MessagePack;
import org.msgpack.core.MessageUnpacker;
import org.msgpack.value.Value;

import java.io.IOException;
import java.net.URI;
import java.nio.ByteBuffer;
import java.util.logging.Logger;

public class WSClient extends WebSocketClient {
    private static boolean debug = true;

    public WSClient(URI serverUri) {
        super(serverUri);
    }

    public WSClient(URI serverUri, Draft protocolDraft) {
        super(serverUri, protocolDraft);
    }

    @Override
    public void onOpen(ServerHandshake serverHandshake) {
        Client.connected.set(true);
        send(ClientToServer.CTS_USERNAME, Client.userName.get().getBytes());
        // TODO: setup pacemaker here
        ImGuiNotify.info(String.format("Successfully connected to %s", Client.host));
    }

    @Override
    public void onMessage(ByteBuffer bytes) {
        try {
            parsePacket(bytes);
        } catch (IOException e) {
            e.printStackTrace();
            ImGuiNotify.error("parse packet: " + e.getMessage());
        }
    }

    @Override
    public void onMessage(String s) {
        ImGuiNotify.info("Received: " + s);
        System.out.println("received " + s);
    }

    @Override
    public void onClose(int i, String s, boolean b) {
        Client.destroy();
    }

    @Override
    public void onError(Exception e) {
        e.printStackTrace();
        ImGuiNotify.error(String.format("Connection to %s failed", Client.host));
        Client.destroy();
    }

    public void send(ClientToServer id, byte[] data) {
        if (this.isOpen() && Client.connected.get()) {
            byte[] out = new byte[data.length + 1];
            out[0] = ((byte) id.getValue());
            System.arraycopy(data, 0, out, 1, data.length);
            super.send(out);
        }
    }

    private void parsePacket(ByteBuffer bytes) throws IOException {
        char id = ((char) bytes.get());
        ServerToClient ev = ServerToClient.from(id);
        byte[] data = new byte[bytes.remaining()];
        bytes.get(data, 0, data.length);
        Logger.getGlobal().info("Received: " + new String(data));
        Value value;
        try (MessageUnpacker unpacker = MessagePack.newDefaultUnpacker(data)) {
            value = unpacker.unpackValue();
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
        switch (ev) {
            case STC_PLAYERS_SCORE -> {
                ScoreMessage scoreMessage = new ScoreMessage(value);
                if (!Client.state.getPeers().containsKey(scoreMessage.getPlayer())) {
                    Logger.getGlobal().severe("[!] Player not found for score update");
                    return ;
                }
                Client.state.getPeers().get(scoreMessage.getPlayer()).setScore(scoreMessage.getScore());
            }
            case STC_PLAYERS_READY_UPDATE -> {
                Logger.getGlobal().info("[+] Got updated ready status");
                Client.updateReadyState(value);
            }
            case STC_SELECTED_CHART_RANDOM -> {
                Logger.getGlobal().info("[+] Received selected bms");
                SelectedBMSMessage selectedBMSMessage = new SelectedBMSMessage(value);

                Client.state.getSelectedSongRemote().setTitle(selectedBMSMessage.getTitle());
                Client.state.getSelectedSongRemote().setMd5(selectedBMSMessage.getMd5());
                Client.state.getSelectedSongRemote().setArtist(selectedBMSMessage.getArtist());

                Lobby.addToLog(String.format("[#] Selected song: %s / %s", selectedBMSMessage.getTitle(), selectedBMSMessage.getArtist()));
                Lobby.addToLog(String.format("[#] Hash: %s", selectedBMSMessage.getMd5()));

                // TODO: Setup item here
                //	hooks::maniac::itemModeEnabled = selectedBms.itemModeEnabled;
                //	if (selectedBms.itemModeEnabled) {
                //		gui::main_window::AddToLog("[#] Item mode enabled!");
                //	}

                // TODO: Setup same random seed here
                //	if (!(state.host == state.remoteId)) { // If not host (!= is not overloaded!!!)
                //		std::cout << "[+] Received random" << std::endl;
                //		hooks::random::received_random = true;
                //		hooks::random::current_seed = selectedBms.randomSeed;
                //	}
                SongDatabaseAccessor songDataAccessor = MainLoader.getScoreDatabaseAccessor();
                String[] queryHash = new String[1];
                queryHash[0] = selectedBMSMessage.getMd5();
                SongData[] songDatas = songDataAccessor.getSongDatas(queryHash);
                if (songDatas.length == 0) {
                    Client.state.getSelectedSongRemote().setPath("");
                    Client.state.setCurrentSongData(null);
                    Lobby.addToLog("[!] You do not have this chart!");
                    send(ClientToServer.CTS_MISSING_CHART, "".getBytes());
                } else {
                    Client.state.setCurrentSongData(songDatas[0]);
                    Client.state.getSelectedSongRemote().setPath(songDatas[0].getPath());
                }
            }
            case STC_USERLIST -> {
                Client.updatePeerState(value);
            }
            case STC_CLIENT_REMOTE_ID -> Client.state.setRemoteId(new Address(value));
            case STC_MESSAGE -> {
                Message message = new Message(value);
                if (message.isSystemMessage()) {
                    Lobby.addToLog(message.getMessage());
                } else {
                    if (!Client.state.getPeers().containsKey(message.getPlayer())) {
                        Logger.getGlobal().info("[!] Player not found for message");
                        return ;
                    }
                    Lobby.addToLogWithUser(message.getMessage(), message.getPlayer());
                }
            }
            /*case STC_MISSING_CHART -> {}*/
            case STC_ITEM -> {
                // TODO: Item
            }
            case STC_ITEM_SETTINGS -> {
                // TODO: Item
            }
            default -> ImGuiNotify.warning("unexpected S->C message id: " + id);
        }
    }
}
