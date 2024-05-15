package br.com.sysmap.domain.service.integration;

import br.com.sysmap.domain.mapper.AlbumMapper;
import br.com.sysmap.domain.model.AlbumModel;
import com.neovisionaries.i18n.CountryCode;
import org.apache.hc.core5.http.ParseException;
import org.springframework.stereotype.Service;
import se.michaelthelin.spotify.exceptions.SpotifyWebApiException;
import se.michaelthelin.spotify.requests.authorization.client_credentials.ClientCredentialsRequest;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;

@Service
public class SpotifyApi {

    private se.michaelthelin.spotify.SpotifyApi spotifyApi = new se.michaelthelin.spotify.SpotifyApi.Builder()
            .setClientId("1ca5b128d6e04b318f3e2b8985a9e040")
            .setClientSecret("47ca1ff20d8c4f8693ca3e7937af5f4f")
            .build();

    public List<AlbumModel> getAlbums(String search) throws IOException, ParseException, SpotifyWebApiException {
        spotifyApi.setAccessToken(getAccessToken());
        return AlbumMapper.INSTACE.toModel(spotifyApi.searchAlbums(search).market(CountryCode.BR)
                .limit(30)
                .build().execute().getItems()).stream()
                .peek(album -> album.setValue(BigDecimal.valueOf(Math.random() * (88 + 1) + 12).
                        setScale(2, BigDecimal.ROUND_HALF_UP))).toList();
    }

    public String getAccessToken() throws IOException, ParseException, SpotifyWebApiException {
        ClientCredentialsRequest clientCredentialsRequest = spotifyApi.clientCredentials().build();
        return clientCredentialsRequest.execute().getAccessToken();
    }
}
