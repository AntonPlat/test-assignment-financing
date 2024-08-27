package lu.crx.financing.repositories;

import lu.crx.financing.entities.Invoice;
import lu.crx.financing.entities.enums.InvoiceStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, Long> {
    long countByStatusAndMaturityDateAfter(InvoiceStatus status, LocalDate date);

    List<Invoice> findByStatusAndMaturityDateAfter(InvoiceStatus status, LocalDate date, Pageable pageable);

}
