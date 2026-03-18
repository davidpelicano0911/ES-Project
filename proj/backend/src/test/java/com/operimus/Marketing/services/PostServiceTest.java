package com.operimus.Marketing.services;

import com.operimus.Marketing.dto.PostDTO;
import com.operimus.Marketing.entities.*;
import com.operimus.Marketing.repositories.PostPlatformRepository;
import com.operimus.Marketing.repositories.PostRepository;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.Resource;
import org.springframework.mock.web.MockMultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PostServiceTest {

    @Mock
    private PostRepository postRepository;

    @InjectMocks
    private PostService postService;

    @Mock
    private PostPlatformRepository postPlatformRepository;

    @Mock
    private FacebookApiService facebookApiService;

    private Post post;

    @BeforeEach
    void setup() {
        post = new Post();
        post.setId(1L);
        post.setName("Sample Post");
        post.setDescription("A test post");
        post.setFile_path("image.jpg");
        post.setScheduled_date(LocalDateTime.now().plusDays(1));
        post.setPlatforms(new ArrayList<>(List.of(new FacebookPlatform())));
    }

    @Test
    void whenGetAllPosts_thenReturnList() {
        when(postRepository.findAll()).thenReturn(List.of(post));

        List<Post> result = postService.getAllPosts();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Sample Post");
        verify(postRepository, times(1)).findAll();
    }

    @Test
    void whenGetPostById_thenReturnPost() {
        when(postRepository.findById(1L)).thenReturn(Optional.of(post));

        Post found = postService.getPostById(1L);

        assertThat(found).isNotNull();
        assertThat(found.getId()).isEqualTo(1L);
        verify(postRepository).findById(1L);
    }

    @Test
    void whenGetPostById_withInvalidId_thenReturnNull() {
        when(postRepository.findById(99L)).thenReturn(Optional.empty());

        Post result = postService.getPostById(99L);

        assertThat(result).isNull();
        verify(postRepository).findById(99L);
    }

    @Test
    void whenCreatePost_withValidData_thenSaveAndReturnPost() throws IOException {
        MockMultipartFile image = new MockMultipartFile("image", "test.jpg", "image/jpeg", "fake-image".getBytes());

        PostDTO dto = new PostDTO();
        dto.setName("New Post");
        dto.setDescription("A new one");
        dto.setScheduled_date(LocalDateTime.now().plusDays(1));
        dto.setPlatforms(List.of("FACEBOOK"));
        dto.setImage(image);

        when(postRepository.save(any(Post.class))).thenAnswer(i -> {
            Post saved = i.getArgument(0);
            saved.setId(10L);
            return saved;
        });

        Post saved = postService.createPost(dto);

        assertThat(saved).isNotNull();
        assertThat(saved.getName()).isEqualTo("New Post");
        assertThat(saved.getPlatforms()).hasSize(1);
        assertThat(saved.getFile_path()).isEqualTo("test.jpg");
        verify(postRepository).save(any(Post.class));
    }

    @Test
    void whenCreatePost_withPastScheduledDate_thenThrowException() {
        PostDTO dto = new PostDTO();
        dto.setName("Old Post");
        dto.setDescription("Invalid");
        dto.setScheduled_date(LocalDateTime.now().minusDays(1));
        dto.setPlatforms(List.of("FACEBOOK"));
        dto.setImage(new MockMultipartFile("image", "t.jpg", "image/jpeg", "x".getBytes()));

        assertThatThrownBy(() -> postService.createPost(dto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Scheduled date must be in the future");
    }

    @Test
    void whenCreatePost_withNoPlatforms_thenThrowException() {
        PostDTO dto = new PostDTO();
        dto.setName("No Platforms");
        dto.setDescription("Missing platforms");
        dto.setImage(new MockMultipartFile("image", "a.jpg", "image/jpeg", "data".getBytes()));

        assertThatThrownBy(() -> postService.createPost(dto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Cannot schedule a post without platforms");
    }

    @Test
    void whenCreatePost_withNoImage_thenCreatesPostSuccessfully() {
        PostDTO dto = new PostDTO();
        dto.setName("No Image");
        dto.setDescription("Missing image");
        dto.setPlatforms(List.of("FACEBOOK"));

        when(postRepository.save(any(Post.class))).thenAnswer(i -> {
            Post p = i.getArgument(0);
            p.setId(1L);
            return p;
        });

        Post result = postService.createPost(dto);

        assertThat(result).isNotNull();
        assertThat(result.getFile_path()).isNull();
    }


    @Test
    void whenUpdatePost_withValidId_thenUpdateFields() throws IOException {
        when(postRepository.findById(1L)).thenReturn(Optional.of(post));
        when(postRepository.save(any(Post.class))).thenAnswer(i -> i.getArgument(0));

        MockMultipartFile newImage = new MockMultipartFile("image", "new.jpg", "image/jpeg", "data".getBytes());

        PostDTO dto = new PostDTO();
        dto.setName("Updated");
        dto.setDescription("Updated desc");
        dto.setScheduled_date(LocalDateTime.now().plusDays(2));
        dto.setPlatforms(List.of("INSTAGRAM"));
        dto.setImage(newImage);

        Post updated = postService.updatePost(1L, dto);

        assertThat(updated.getName()).isEqualTo("Updated");
        assertThat(updated.getDescription()).isEqualTo("Updated desc");
        assertThat(updated.getPlatforms().get(0)).isInstanceOf(InstagramPlatform.class);
        assertThat(updated.getFile_path()).isEqualTo("new.jpg");
        verify(postRepository).save(any(Post.class));
    }

    @Test
    void whenUpdatePost_withInvalidId_thenReturnNull() {
        when(postRepository.findById(99L)).thenReturn(Optional.empty());

        PostDTO dto = new PostDTO();
        dto.setName("Ignored");

        Post result = postService.updatePost(99L, dto);

        assertThat(result).isNull();
        verify(postRepository).findById(99L);
        verify(postRepository, never()).save(any());
    }

    @Test
    void whenDeletePost_withExistingId_thenReturnTrue() {
        when(postRepository.existsById(1L)).thenReturn(true);
        doNothing().when(postRepository).deleteById(1L);

        boolean result = postService.deletePost(1L);

        assertThat(result).isTrue();
        verify(postRepository).deleteById(1L);
    }

    @Test
    void whenDeletePost_withNonExistingId_thenReturnFalse() {
        when(postRepository.existsById(99L)).thenReturn(false);

        boolean result = postService.deletePost(99L);

        assertThat(result).isFalse();
        verify(postRepository, never()).deleteById(any());
    }

    @Test
    void whenGetImage_existingFile_thenReturnResource() throws IOException {
        Path uploadDir = Paths.get(System.getProperty("upload.dir", "uploads/"));
        Files.createDirectories(uploadDir);
        Path filePath = uploadDir.resolve("sample.jpg");
        Files.writeString(filePath, "dummy");

        Resource resource = postService.getImage("sample.jpg");

        assertThat(resource).isNotNull();
        assertThat(resource.exists()).isTrue();
    }

    @Test
    void whenGetImage_nonExistingFile_thenReturnNull() {
        Resource resource = postService.getImage("missing.jpg");
        assertThat(resource).isNull();
    }

    @Test
    void whenPublish_thenFacebookPlatformIsPublishedAndSaved() {
        // usar spy para não executar lógica real de publish (que mexe em ficheiros)
        FacebookPlatform fb = Mockito.spy(new FacebookPlatform());
        fb.setPost(post);
        post.setPlatforms(List.of(fb));

        // não queremos que o método real mexa no disco → anulamos o comportamento
        doNothing().when(fb).publish(any(FacebookApiService.class));

        postService.publish(post);

        // garantir que o service chamou publish na plataforma com o service certo
        verify(fb, times(1)).publish(facebookApiService);
        // garantir que o service guardou a plataforma no repositório
        verify(postPlatformRepository, times(1)).save(fb);
    }

}
