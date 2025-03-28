package success.planfit.post.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import success.planfit.entity.like.PostLike;
import success.planfit.entity.post.Post;
import success.planfit.entity.user.User;
import success.planfit.repository.PostLikeRepository;
import success.planfit.repository.PostRepository;
import success.planfit.repository.UserRepository;
import success.planfit.user.service.UserService;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class PostLikeService {

    private final PostLikeRepository postLikeRepository;
    private final UserRepository userRepository;
    private final PostRepository postRepository;

    public void likePost(Long postId, Long userId) {
        User user = userRepository.findById(userId).
                orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 포스팅입니다."));

        postLikeRepository.findByUserIdAndPostId(userId, postId)
                .ifPresent(like -> {
                    throw new IllegalArgumentException("이미 좋아요한 포스트입니다.");
                });

        postLikeRepository.save(PostLike.builder()
                .user(user)
                .post(post)
                .build());
        post.increaseLikeCount();
    }

    @Transactional(readOnly = true)
    public List<Long> getLikedPosts(Long userId) {
        return postLikeRepository.findByUserId(userId).
                stream()
                .map(like -> like.getPost().getId())
                .toList();
    }

    public void unlikePost(Long postId, Long userId) {
        PostLike postLike = postLikeRepository.findByUserIdAndPostId(userId, postId)
                .orElseThrow(() -> new IllegalArgumentException("좋아요하지 않은 포스트입니다."));

        postLikeRepository.delete(postLike);
        postLike.getPost().decreaseLikeCount();
    }

}

