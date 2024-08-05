package lu.crx.financing.services;

import lu.crx.financing.entities.Invoice;
import lu.crx.financing.repositories.InvoiceRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class InvoiceService {

    private final InvoiceRepository invoiceRepository;

    public InvoiceService(InvoiceRepository invoiceRepository) {
        this.invoiceRepository = invoiceRepository;
    }

    public void updateInvoiceAsFinanced(Invoice invoice) {
        invoice.setFinanced(true);
        invoiceRepository.save(invoice);
    }


    public List<Invoice> getAllNotFinancedInvoices(){
        return invoiceRepository.findByFinancedFalse();
    }
}
