
package br.com.sysmap.domain.service;

import br.com.sysmap.domain.entities.Album;
import br.com.sysmap.domain.entities.Users;
import br.com.sysmap.domain.exceptions.AlbumAlreadyBoughtException;
import br.com.sysmap.domain.exceptions.AlbumNotFoundException;
import br.com.sysmap.domain.model.AlbumModel;
import br.com.sysmap.domain.repository.AlbumRepository;
import br.com.sysmap.domain.service.integration.SpotifyApi;
import br.com.sysmap.dto.WalletDto;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.hc.core5.http.ParseException;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import se.michaelthelin.spotify.exceptions.SpotifyWebApiException;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;


@RequiredArgsConstructor
@Slf4j
@Service
public class AlbumService {

    private final ObjectMapper objectMapper;
    private final Queue queue;
    private final RabbitTemplate template;
    private final SpotifyApi spotifyApi;
    private final AlbumRepository albumRepository;
    private final UsersService usersService;

    @Transactional(propagation = Propagation.REQUIRED)
    public List<AlbumModel> getAlbums(String search) throws IOException, ParseException, SpotifyWebApiException {
        return this.spotifyApi.getAlbums(search);
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public Album albumSale(Album album) throws JsonProcessingException {
        try {
            Users user = getUser();
            List<Album> userAlbums = albumRepository.findAllByUsers(user);
            for (Album userAlbum : userAlbums) {
                if (userAlbum.getIdSpotify().equals(album.getIdSpotify())) {
                    throw new AlbumAlreadyBoughtException("Album with Spotify ID " + album.getIdSpotify() + " already bought by the user.");
                }
            }

            album.setUsers(user);
            Album albumSold = albumRepository.save(album);

            BigDecimal albumValue = albumSold.getValue();
            String userEmail = user.getEmail();
            WalletDto walletDto = new WalletDto(userEmail, albumValue);

            this.template.convertAndSend(queue.getName(), objectMapper.writeValueAsString(walletDto));

            return albumSold;
        } catch (AlbumAlreadyBoughtException e) {
            throw new RuntimeException("Album already bought", e);
        }
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public List<Album> getAllUserAlbums(String userEmail) {
        try {
            Users user = usersService.findByEmail(userEmail);
            return albumRepository.findAllByUsers(user);
        } catch (Exception e) {
            throw new RuntimeException("Error retrieving user albums", e);
        }
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public void removeAlbumById(Long id) {
        try {
            if (!albumRepository.existsById(id)) {
                throw new AlbumNotFoundException("Album with Id: " + id + " was not found.");
            }
            albumRepository.deleteById(id);
        } catch (AlbumNotFoundException e) {
            throw new RuntimeException("Album not found", e);
        }
    }

    private Users getUser() {
        String username = SecurityContextHolder.getContext().getAuthentication()
                .getPrincipal().toString();
        return usersService.findByEmail(username);
    }
}
