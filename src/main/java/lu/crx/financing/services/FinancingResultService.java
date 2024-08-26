package lu.crx.financing.services;

import lu.crx.financing.entities.FinancingResult;
import lu.crx.financing.repositories.FinancingResultRepository;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;

@Service
public class FinancingResultService {

    private final FinancingResultRepository financingResultRepository;

    public FinancingResultService(FinancingResultRepository financingResultRepository) {
        this.financingResultRepository = financingResultRepository;
    }

    @Transactional
    public void saveFinancingResultsBatch(List<FinancingResult> results) {
        financingResultRepository.saveAll(results);
    }
}
