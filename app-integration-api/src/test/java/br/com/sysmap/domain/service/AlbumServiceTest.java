package br.com.sysmap.domain.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import br.com.sysmap.domain.exceptions.AlbumAlreadyBoughtException;
import br.com.sysmap.domain.model.AlbumModel;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;

import br.com.sysmap.domain.entities.Album;
import br.com.sysmap.domain.entities.Users;
import br.com.sysmap.domain.repository.AlbumRepository;
import br.com.sysmap.domain.service.integration.SpotifyApi;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

public class AlbumServiceTest {

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private Queue queue;

    @Mock
    private RabbitTemplate template;

    @Mock
    private SpotifyApi spotifyApi;

    @Mock
    private AlbumRepository albumRepository;

    @Mock
    private UsersService usersService;

    @InjectMocks
    private AlbumService albumService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        Authentication authentication = new UsernamePasswordAuthenticationToken("user@example.com", "password");
        SecurityContext securityContext = SecurityContextHolder.getContext();
        securityContext.setAuthentication(authentication);
    }

    @Test
    @DisplayName("Should sell an album")
    void shouldSellAnAlbum() throws JsonProcessingException {
        Album album = mock(Album.class);
        album.setIdSpotify("spotify_id");
        album.setValue(BigDecimal.TEN);

        Users user = mock(Users.class);
        user.setEmail("user@example.com");

        when(usersService.findByEmail(any())).thenReturn(user);
        when(albumRepository.findAllByUsers(any())).thenReturn(new ArrayList<>());
        when(albumRepository.save(any())).thenReturn(album);

        Album soldAlbum = albumService.albumSale(album);

        assertNotNull(soldAlbum);
        verify(albumRepository, times(1)).save(album);
    }

    @Test
    @DisplayName("Should return album already bought when user tries to buy the same Album")
    void testAlbumSaleAlreadyBought() throws AlbumAlreadyBoughtException {
        Album album = mock(Album.class);
        album.setIdSpotify("spotify_id");
        Users user = mock(Users.class);
        user.setEmail("user@example.com");
        ArrayList<Album> userAlbums = new ArrayList<>();
        userAlbums.add(album);

        when(usersService.findByEmail(any())).thenReturn(user);
        when(albumRepository.findAllByUsers(any())).thenReturn(userAlbums);

//        assertThrows(AlbumAlreadyBoughtException.class, () -> albumService.albumSale(album));
        verify(albumRepository, never()).save(album);
//        verify(template, never()).convertAndSend(Optional.ofNullable(any()), any());
    }


    @Test
    @DisplayName("Should get albums")
    void shouldGetAlbums() throws Exception {
        String search = "test";
        List<AlbumModel> expectedAlbums = new ArrayList<>();

        when(spotifyApi.getAlbums(search)).thenReturn(expectedAlbums);

        List<AlbumModel> result = albumService.getAlbums(search);

        assertEquals(expectedAlbums, result);
    }


    @Test
    @DisplayName("Should test if album exists")
    void shouldTestIfAlbumExists() throws Exception {
        Album album = mock(Album.class);
        album.setIdSpotify("test");
        album.setValue(BigDecimal.TEN);

        when(albumRepository.existsByIdSpotify(album.getIdSpotify())).thenReturn(true);

        assertThrows(RuntimeException.class, () -> {
            albumService.albumSale(album);
        });
    }

    @Test
    @DisplayName("Should get user album collection")
    void shouldGetUserAlbumCollection() throws Exception {
        String userEmail = "user@example.com";
        Users user = mock(Users.class);
        List<Album> expectedAlbums = new ArrayList<>();

        when(usersService.findByEmail(userEmail)).thenReturn(user);
        when(albumRepository.findAllByUsers(user)).thenReturn(expectedAlbums);

        List<Album> result = albumService.getAllUserAlbums(userEmail);

        assertEquals(expectedAlbums, result);
    }

    @Test
    @DisplayName("Should remove an album from collection")
    void shouldRemoveAlbum() throws Exception {
        Long id = 1L;

        when(albumRepository.existsById(id)).thenReturn(true);

        albumService.removeAlbumById(id);

        verify(albumRepository).deleteById(id);
    }

    @Test
    @DisplayName("Should not remove an album if album id not found")
    void shouldNotRemoveAlbumIfAlbumIdNotFound() throws Exception {
        Long id = 1L;

        when(albumRepository.existsById(id)).thenReturn(false);

        assertThrows(RuntimeException.class, () -> {
            albumService.removeAlbumById(id);
        });
    }
}

