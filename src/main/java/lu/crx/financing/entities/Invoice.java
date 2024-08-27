package lu.crx.financing.entities;

import lombok.*;
import lu.crx.financing.entities.enums.InvoiceStatus;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDate;

/**
 * An invoice issued by the {@link Creditor} to the {@link Debtor} for shipped goods.
 */
@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "invoice", indexes = {
        @Index(name = "idx_invoice_status", columnList = "status"),
        @Index(name = "idx_invoice_maturity_date", columnList = "maturity_date")
})
public class Invoice implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    /**
     * Creditor is the entity that issued the invoice.
     */
    @ManyToOne(optional = false)
    private Creditor creditor;

    /**
     * Debtor is the entity obliged to pay according to the invoice.
     */
    @ManyToOne
    private Debtor debtor;

    /**
     * Maturity date is the date on which the {@link #debtor} is to pay for the invoice.
     * In case the invoice was financed, the money will be paid in full on this date to the purchaser of the invoice.
     */
    @Basic(optional = false)
    @Column(name = "maturity_date", nullable = false)
    private LocalDate maturityDate;

    /**
     * The value is the amount to be paid for the shipment by the Debtor.
     */
    @Basic(optional = false)
    private long valueInCents;

    /**
     * Indicates the current status of the invoice.
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private InvoiceStatus status;
}
