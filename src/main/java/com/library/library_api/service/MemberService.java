package com.library.library_api.service;

// FIX: removed unused imports `BookResponse` and `Book` - leftovers from copying
// BookService. Unused imports aren't a runtime bug, but they're clutter and a sign a
// copy-paste wasn't fully cleaned up. (IntelliJ greys them out - a handy hint.)
import com.library.library_api.dto.MemberRequest;
import com.library.library_api.dto.MemberResponse;
import com.library.library_api.entity.Member;
import com.library.library_api.exception.MemberNotFoundException;
import com.library.library_api.repository.MemberRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class MemberService {

    // FIX: added `final`. The repository is set once in the constructor and should never
    // be reassigned - marking it final makes that guarantee explicit (matches BookService).
    private final MemberRepository memberRepository;

    // Good call on constructor injection - and the reasoning in your comment was correct.
    public MemberService(MemberRepository memberRepository){
        this.memberRepository = memberRepository;
    }

    public MemberResponse createMember(MemberRequest memberRequest){
        Member member = new Member();
        member.setEmail(memberRequest.getEmail());
        member.setName(memberRequest.getName());
        member.setPhone(memberRequest.getPhone());
        // FIX: was `member.setMembershipDate(memberRequest.getMembershipDate())`.
        // membershipDate no longer comes from the request - the SERVER decides it. A new
        // member joins "now", so we stamp today's date here. This is the whole reason we
        // dropped the field from MemberRequest.
        member.setMembershipDate(LocalDate.now());

        return toResponse(memberRepository.save(member));
    }

    public MemberResponse getMember(Long id){
        Member member = memberRepository.findById(id)
                .orElseThrow(() -> new MemberNotFoundException(id));
        return toResponse(member);
    }

    public List<MemberResponse> getAllMembers(){
        return memberRepository.findAll().stream()
                .map(this::toResponse).toList();
    }

    public MemberResponse updateMember(Long id, MemberRequest memberRequest){
        Member member = memberRepository.findById(id).orElseThrow(() -> new MemberNotFoundException(id));

        // FIX: removed the `setMembershipDate(...)` line that was here.
        // membershipDate records WHEN they joined - that's history and must never change on
        // an update. We load the existing member and only overwrite the editable fields,
        // leaving the original join date intact.
        member.setPhone(memberRequest.getPhone());
        member.setName(memberRequest.getName());
        member.setEmail(memberRequest.getEmail());

        return toResponse(memberRepository.save(member));
    }

    public void deleteMember(Long id){
        if(!memberRepository.existsById(id)){
            throw new MemberNotFoundException(id);
        }
        memberRepository.deleteById(id);
    }

    // Converts an entity into the shape we expose over the API.
    private MemberResponse toResponse(Member member) {
        MemberResponse response = new MemberResponse();
        // FIX: added this line - now that MemberResponse has an id, we must actually copy
        // it across, otherwise every response would come back with id = null.
        response.setId(member.getId());
        response.setEmail(member.getEmail());
        response.setName(member.getName());
        response.setMembershipDate(member.getMembershipDate());
        response.setPhone(member.getPhone());

        return response;
    }

}
