package com.example.spring_doc.domain.post.comment.entity;

import com.example.spring_doc.domain.member.member.entity.Member;
import com.example.spring_doc.domain.post.post.entity.Post;
import com.example.spring_doc.global.entity.BaseTime;
import com.example.spring_doc.global.exception.ServiceException;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ManyToOne;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Entity
@Getter
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public class Comment extends BaseTime {

    private String content;

    @ManyToOne(fetch = FetchType.LAZY)
    private Post post;

    @ManyToOne(fetch = FetchType.LAZY)
    private Member author;

    public void modify(String content) {
        this.content = content;
    }

    public boolean canModify(Member writer) {
        if (writer == null) {
            throw new ServiceException("401-1", "인증 정보가 없습니다.");
        }

        if (writer.isAdmin() || writer.equals(this.getAuthor())) {
            return true;
        }

        throw new ServiceException("403-1", "자신이 작성한 댓글만 수정 가능합니다.");
    }

    public boolean canDelete(Member writer) {
        if (writer == null) {
            throw new ServiceException("401-1", "인증 정보가 없습니다.");
        }

        if (writer.isAdmin() || writer.equals(this.getAuthor())) {
            return true;
        }

        throw new ServiceException("403-1", "자신이 작성한 댓글만 수정 가능합니다.");
    }


}
