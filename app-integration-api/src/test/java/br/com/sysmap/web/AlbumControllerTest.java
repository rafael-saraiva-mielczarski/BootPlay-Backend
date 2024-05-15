package br.com.sysmap.web;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import br.com.sysmap.domain.entities.Album;
import br.com.sysmap.domain.model.AlbumModel;
import br.com.sysmap.domain.service.AlbumService;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.apache.hc.core5.http.ParseException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import se.michaelthelin.spotify.SpotifyApi;
import se.michaelthelin.spotify.exceptions.SpotifyWebApiException;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

class AlbumControllerTest {

    @Mock
    private SpotifyApi spotifyApi;

    @Mock
    private AlbumService albumService;

    @InjectMocks
    private AlbumControler albumControler;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("Should get Albums")
    void shouldGetAlbums() throws IOException, ParseException, SpotifyWebApiException {
        when(albumService.getAlbums("search term")).thenReturn(Collections.emptyList());

        ResponseEntity<List<AlbumModel>> response = albumControler.getAlbums("search term");

        assertEquals(200, response.getStatusCodeValue());
        assertTrue(response.getBody().isEmpty());
    }

    @Test
    @DisplayName("Should sell an Album")
    void shouldSellAnAlbum() throws JsonProcessingException {
        Album album = mock(Album.class);
        when(albumService.albumSale(album)).thenReturn(album);

        ResponseEntity<Album> response = albumControler.albumSale(album);

        assertEquals(200, response.getStatusCodeValue());
        assertEquals(album, response.getBody());
    }

    @Test
    @DisplayName("Should get user collection")
    void shouldGetUserCollection() throws JsonProcessingException {
        Authentication authentication = new UsernamePasswordAuthenticationToken("user@example.com", "password");
        SecurityContext securityContext = SecurityContextHolder.getContext();
        securityContext.setAuthentication(authentication);

        List<Album> userAlbums = Collections.emptyList();
        when(albumService.getAllUserAlbums("user@example.com")).thenReturn(userAlbums);

        ResponseEntity<List<Album>> response = albumControler.getUserAlbums();

        assertEquals(200, response.getStatusCodeValue());
        assertEquals(userAlbums, response.getBody());
    }

    @Test
    @DisplayName("Should remove Album by id from collection")
    void shouldRemoveAlbumById() throws JsonProcessingException {
        assertDoesNotThrow(() -> albumControler.removeAlbumById(1L));
    }
}

