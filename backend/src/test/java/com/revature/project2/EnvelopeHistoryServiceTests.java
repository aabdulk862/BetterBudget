package com.revature.project2;

import com.revature.project2.models.EnvelopeHistory;
import com.revature.project2.repositories.EnvelopeHistoryRepository;
import com.revature.project2.services.EnvelopeHistoryService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.when;


class EnvelopeHistoryServiceTests {
    private EnvelopeHistoryRepository envelopeHistoryRepository;
    private EnvelopeHistoryService envelopeHistoryService;

    @BeforeEach
    void contextLoads() {
        envelopeHistoryRepository = Mockito.mock(EnvelopeHistoryRepository.class);
        envelopeHistoryService = new EnvelopeHistoryService(envelopeHistoryRepository);
    }

    @Test
    void test_getAllEnvelopeHistory(){
        List<EnvelopeHistory> mockedList = new ArrayList<>();
        EnvelopeHistory envelopeHistory = new EnvelopeHistory();
        mockedList.add(new EnvelopeHistory());
        Pageable pageable = PageRequest.of(0, 20);
        Page<EnvelopeHistory> mockedPage = new PageImpl<>(mockedList, pageable, mockedList.size());
        when(envelopeHistoryRepository.findAll(pageable)).thenReturn(mockedPage);
        Page<EnvelopeHistory> outputResponse = envelopeHistoryService.getAllEnvelopeHistory(pageable);
        Assertions.assertEquals(1, outputResponse.getTotalElements());
        Assertions.assertEquals(1, outputResponse.getContent().size());
        Assertions.assertTrue(envelopeHistory.toString().equals(outputResponse.getContent().get(0).toString()));

        Mockito.verify(envelopeHistoryRepository).findAll(pageable);
        Mockito.verifyNoMoreInteractions(envelopeHistoryRepository);
    }

    @Test
    void test_getEnvelopeHistoryByEnvelopeId_idFound(){
        List<EnvelopeHistory> mockedList = new ArrayList<>();
        EnvelopeHistory envelopeHistory = new EnvelopeHistory();
        mockedList.add(new EnvelopeHistory());
        when(envelopeHistoryRepository.findByEnvelope_EnvelopeId(0)).thenReturn(mockedList);
        List<EnvelopeHistory> outputResponse = envelopeHistoryService.getEnvelopeHistoryByEnvelopeId(0);
        Assertions.assertEquals(1, outputResponse.size());
        Assertions.assertTrue(envelopeHistory.toString().equals(outputResponse.get(0).toString()));
    }

    @Test
    void test_createEnvelopeHistory(){
        EnvelopeHistory envelopeHistory = new EnvelopeHistory();
        when(envelopeHistoryRepository.save(envelopeHistory)).thenReturn(envelopeHistory);
        EnvelopeHistory outputEnvelopeHistory = envelopeHistoryService.createEnvelopeHistory(envelopeHistory);
        Assertions.assertTrue(outputEnvelopeHistory.toString().equals(envelopeHistory.toString()));
    }
}
