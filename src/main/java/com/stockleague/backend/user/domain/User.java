package com.stockleague.backend.user.domain;

import com.stockleague.backend.notification.domain.Notification;
import com.stockleague.backend.stock.domain.Comment;
import com.stockleague.backend.stock.domain.CommentLike;
import com.stockleague.backend.stock.domain.CommentReport;
import com.stockleague.backend.stock.domain.Watchlist;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long id;

    @Column(name = "oauth_id", nullable = false, unique = true)
    private String oauthId;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    private UserRole role;

    @Column(name = "nickname", nullable = false, length = 10)
    private String nickname;

    @Column(name = "agreed_to_terms", nullable = false)
    private Boolean agreedToTerms;

    @Column(name = "is_over_fifteen", nullable = false)
    private Boolean isOverFifteen;

    @Enumerated(EnumType.STRING)
    @Column(name = "provider", nullable = false)
    private OauthServerType provider;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Builder.Default
    @Column(name = "warning_count", nullable = false)
    private Integer warningCount = 0;

    @Builder.Default
    @Column(name = "is_banned", nullable = false)
    private Boolean isBanned = false;

    @Column(name = "banned_at")
    private LocalDateTime bannedAt;

    @Column(name = "ban_reason")
    private String banReason;

    @Builder.Default
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Comment> comments = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CommentLike> commentLikes = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "reporter", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CommentReport> reportsFiled = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "processedBy", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CommentReport> reportsProcessed = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Notification> notifications = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "warnedUser")
    private List<UserWarning> receivedWarnings = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "admin")
    private List<UserWarning> issuedWarnings = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "processedBy", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Comment> processedAdmin = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Watchlist> watchlists = new ArrayList<>();

    // 비즈니스 메서드
    public void updateNickname(String newNickname) {
        this.nickname = newNickname;
    }

    public void increaseWarningCount() {
        this.warningCount++;
    }

    public void decreaseWarningCount() {
        if (this.warningCount > 0) {
            this.warningCount--;
        }
    }

    public void ban(String reason) {
        this.isBanned = true;
        this.bannedAt = LocalDateTime.now();
        this.banReason = reason;
    }
}
