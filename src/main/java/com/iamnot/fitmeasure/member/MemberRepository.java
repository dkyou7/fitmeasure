package com.iamnot.fitmeasure.member;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MemberRepository extends JpaRepository<Member, Long> {

    @Query("""
            select m from Member m
            where (:kw is null or :kw = ''
                   or m.name like %:kw%
                   or m.phone like %:kw%)
            order by m.id desc
            """)
    Page<Member> searchByNameOrPhone(@Param("kw") String kw, Pageable pageable);
}