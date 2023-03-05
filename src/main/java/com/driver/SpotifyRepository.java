package com.driver;

import java.util.*;

import org.springframework.stereotype.Repository;

@Repository
public class SpotifyRepository {
    public HashMap<Artist, List<Album>> artistAlbumMap;
    public HashMap<Album, List<Song>> albumSongMap;
    public HashMap<Playlist, List<Song>> playlistSongMap;
    public HashMap<Playlist, List<User>> playlistListenerMap;
    public HashMap<User, Playlist> creatorPlaylistMap;
    public HashMap<User, List<Playlist>> userPlaylistMap;
    public HashMap<Song, List<User>> songLikeMap;

    public List<User> users;
    public List<Song> songs;
    public List<Playlist> playlists;
    public List<Album> albums;
    public List<Artist> artists;

    public SpotifyRepository(){
        //To avoid hitting apis multiple times, initialize all the hashmaps here with some dummy data
        artistAlbumMap = new HashMap<>();
        albumSongMap = new HashMap<>();
        playlistSongMap = new HashMap<>();
        playlistListenerMap = new HashMap<>();
        creatorPlaylistMap = new HashMap<>();
        userPlaylistMap = new HashMap<>();
        songLikeMap = new HashMap<>();

        users = new ArrayList<>();
        songs = new ArrayList<>();
        playlists = new ArrayList<>();
        albums = new ArrayList<>();
        artists = new ArrayList<>();
    }

    public User createUser(String name, String mobile) {
        User user = new User(name,mobile);
        users.add(user);                                    // added new user to user list
        userPlaylistMap.put(user,new ArrayList<>());
        return user;
    }

    public Artist createArtist(String name) {
        Artist artist = new Artist(name);
        artists.add(artist);                                // added new artist to artist list
        artistAlbumMap.put(artist,new ArrayList<>());
        return artist;
    }

    public Album createAlbum(String title, String artistName) {
        Album album = new Album(title);
        Artist artist = null;

        for(Artist artist1 : artists){
            if(artist1.getName().equals(artistName)){
                artist = artist1;                      // artist found
                break;
            }
        }
        // artist does not exist
        if(artist == null)
            artist = createArtist(artistName); // create new artist

        album.setTitle(title);
        album.setReleaseDate(new Date());

        albums.add(album);

        albumSongMap.put(album,new ArrayList<>());
        artistAlbumMap.get(artist).add(album);

        return album;
    }

    public Song createSong(String title, String albumName, int length) throws Exception{
       Song newSong = new Song(title,length);
       Album album = null;

       for(Album album1 : albums){
           if(album1.getTitle().equals(albumName)){
               album = album1;                     // album exists
               break;
           }
       }
       // If the album does not exist, it throws an exception.
       if(album == null)
           throw new Exception("Album does not exist");

       newSong.setLikes(0);

       songs.add(newSong);

       songLikeMap.put(newSong, new ArrayList<>());
       albumSongMap.get(album).add(newSong);

       return newSong;
    }

    public Playlist createPlaylistOnLength(String mobile, String title, int length) throws Exception {
        // creates a new playlist with a given title and adds all songs with a given length to the playlist.
        // The creator of the playlist is the given user, who is also the only listener at the time of playlist creation.
        // If the user does not exist, it throws an exception.

        Playlist playlist = new Playlist(title);

        User user = null;
        for(User user1 : users){
            if(user1.getMobile().equals(mobile)){
                user = user1;                      // user exists
                break;
            }
        }
        // If the user does not exist, it throws an exception.
        if(user == null)
            throw new Exception("User does not exist");

        playlist.setTitle(title);
        playlists.add(playlist);

        //all song list
        List<Song> songList = new ArrayList<>();
        for(Song song : songs){
            if(song.getLength() == length){
                songList.add(song);
            }
        }

        playlistSongMap.put(playlist,songList);
        List<User> listener = new ArrayList<>();
        listener.add(user);

        playlistListenerMap.put(playlist,listener);

        creatorPlaylistMap.put(user,playlist);

        userPlaylistMap.get(user).add(playlist);

        return playlist;
    }

    public Playlist createPlaylistOnName(String mobile, String title, List<String> songTitles) throws Exception {
        //creates a new playlist with a given title and adds all songs with the given titles to the playlist.
        // The creator of the playlist is the given user, who is also the only listener at the time of playlist creation.
        // If the user does not exist, it throws an exception.

        Playlist playlist = new Playlist(title);

        User user = null;
        for(User user1 : users){
            if(user1.getMobile().equals(mobile)){
                user = user1;                      // user exists
                break;
            }
        }
        // If the user does not exist, it throws an exception.
        if(user == null)
            throw new Exception("User does not exist");

        //all song list
        List<Song> songList = new ArrayList<>();
        for(Song song : songs){
            if(songTitles.contains(song.getTitle())){
                songList.add(song);
            }
        }

        playlistSongMap.put(playlist,songList);
        List<User> listener = new ArrayList<>();
        listener.add(user);

        playlistListenerMap.put(playlist,listener);

        creatorPlaylistMap.put(user,playlist);

        userPlaylistMap.get(user).add(playlist);

        playlists.add(playlist);

        return playlist;

    }

    public Playlist findPlaylist(String mobile, String playlistTitle) throws Exception {
        // Finds a playlist with a given title and adds the given user as a listener to that playlist.
        // If the user is the creator or already a listener, it does nothing.

        User user = null;
        for(User user1 : users){
            if(user1.getMobile().equals(mobile)){
                user = user1;                      // user exists
                break;
            }
        }
        if(user == null)
            throw new Exception("User does not exist");

        Playlist playlist = null;
        for(Playlist playlist1 : playlists){
            if(playlist1.getTitle().equals(playlistTitle)){
                playlist = playlist1;                      // user exists
                break;
            }
        }
        // If the user does not exist, it throws an exception.
        if(playlist == null)
            throw new Exception("Playlist does not exist");

        if(creatorPlaylistMap.containsKey(user))
            return playlist;

        List<User> userList = playlistListenerMap.get(playlist);
        if(!userList.contains(user))
            userList.add(user);                 // added listener to playlist

        List<Playlist> playlistList = userPlaylistMap.get(user);
        if(!playlistList.contains(playlist))
            playlistList.add(playlist);         // added playlist to user

        return playlist;
    }

    public Song likeSong(String mobile, String songTitle) throws Exception {
        //This API allows a user to like a given song, which also auto-likes the corresponding artist.
        // If the user has already liked the song, it does nothing.

        User user = null;
        for(User user1 : users){
            if(user1.getMobile().equals(mobile)){
                user = user1;                      // user exists
                break;
            }
        }
        if(user == null)
            throw new Exception("User does not exist");

        Song currSong = null;
        for(Song song : songs){
            if(song.getTitle().equals(songTitle)){
                currSong = song;                      // user exists
                break;
            }
        }
        // If the user does not exist, it throws an exception.
        if(currSong == null)
            throw new Exception("Song does not exist");

        List<User> like = songLikeMap.get(currSong);
        if(!like.contains(user)){
            like.add(user);
            currSong.setLikes(like.size());

            Artist artist = null;
            for(Artist artist1 : artistAlbumMap.keySet()){
                List<Album> albums1 = artistAlbumMap.get(artist1);
                for(Album album : albums1){
                    if(albumSongMap.get(album).contains(currSong)){
                        artist = artist1;
                        artist.setLikes(artist.getLikes()+1);
                    }
                }
            }
        }
        return currSong;
    }

    public String mostPopularArtist() {
        Artist mostPopular = artists.get(0);
        for(Artist artist : artists){
            if(artist.getLikes() > mostPopular.getLikes()){
                mostPopular = artist;
            }
        }
        return mostPopular.getName();
    }

    public String mostPopularSong() {
    Song mostopular = songs.get(0);
    for(Song song : songs){
        if(song.getLikes() > mostopular.getLikes()){
            mostopular = song;
        }
    }
    return mostopular.getTitle();
    }
}
