package com.zipup.server.funding.application;

import com.zipup.server.funding.domain.Fund;
import com.zipup.server.funding.dto.LetterData;
import com.zipup.server.funding.infrastructure.FundRepository;
import com.zipup.server.global.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.zipup.server.global.exception.CustomErrorCode.DATA_NOT_FOUND;
import static com.zipup.server.global.util.UUIDUtil.isValidUUID;

@Service
@RequiredArgsConstructor
public class LetterService {

    private final FundRepository fundRepository;

    @Transactional(readOnly = true)
    public LetterData findById(String fundId) {
        Fund response =  fundRepository.findById(isValidUUID(fundId))
                .orElseThrow(()-> new ResourceNotFoundException(DATA_NOT_FOUND));

        return new LetterData(String.valueOf(response.getId()), response.getLetter());
    }
    public void saveLetter(String fundId, String content) {
        Fund response = fundRepository.findById(isValidUUID(fundId))
                .orElseThrow(()-> new ResourceNotFoundException(DATA_NOT_FOUND));
        response.setLetter(content);
        fundRepository.save(response);
    }
}
