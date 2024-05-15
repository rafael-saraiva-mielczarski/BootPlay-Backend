package br.com.sysmap.web;

import br.com.sysmap.domain.entities.Album;
import br.com.sysmap.domain.model.AlbumModel;
import br.com.sysmap.domain.service.integration.SpotifyApi;
import br.com.sysmap.domain.service.AlbumService;
import com.fasterxml.jackson.core.JsonProcessingException;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.apache.hc.core5.http.ParseException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import se.michaelthelin.spotify.exceptions.SpotifyWebApiException;
import java.io.IOException;
import java.util.List;

@CrossOrigin(origins = "*")
@RequiredArgsConstructor
@RestController
@RequestMapping("/albums")
public class AlbumControler {

    private final SpotifyApi   spotifyApi;


    private final AlbumService albumService;

    @Operation(summary = "Get all albums")
    @GetMapping("/all")
    public ResponseEntity<List<AlbumModel>> getAlbums(@RequestParam("search") String search) throws IOException, ParseException, SpotifyWebApiException {
        return ResponseEntity.ok(this.albumService.getAlbums(search));
    }

    @Operation(summary = "Sell an album to an user")
    @PostMapping("/sale")
    public ResponseEntity<Album> albumSale(@RequestBody Album album) throws JsonProcessingException {
        return ResponseEntity.ok(this.albumService.albumSale(album));
    }

    @Operation(summary = "Get albums of an user")
    @GetMapping("/my-collection")
    public ResponseEntity<List<Album>> getUserAlbums() {
        String userEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        List<Album> userAlbums = albumService.getAllUserAlbums(userEmail);
        return ResponseEntity.ok(userAlbums);
    }

    @Operation(summary = "Delete album by its Id")
    @DeleteMapping("/remove/{id}")
    public void removeAlbumById(@PathVariable Long id) {
        albumService.removeAlbumById(id);
    }
}