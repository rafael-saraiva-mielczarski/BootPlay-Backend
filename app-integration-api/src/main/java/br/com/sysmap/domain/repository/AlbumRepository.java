package br.com.sysmap.domain.repository;

import br.com.sysmap.domain.entities.Album;
import br.com.sysmap.domain.entities.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AlbumRepository extends JpaRepository<Album, Long> {

    List<Album> findAllByUsers(Users users);

    boolean existsByIdSpotify(String idSpotify);

    void deleteById(Long id);
}
