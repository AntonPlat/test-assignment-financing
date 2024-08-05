package lu.crx.financing.entities;

import lombok.*;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDate;

@Entity
@Getter
@Setter
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FinancingResult implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    @OneToOne(optional = false)
    @JoinColumn(name = "invoice_id", unique = true)
    private Invoice invoice;

    @ManyToOne(optional = false)
    private Purchaser purchaser;

    @Basic(optional = false)
    private LocalDate financingDate;

    @Basic(optional = false)
    private int financingRateInBps;

    @Basic(optional = false)
    private long earlyPaymentAmountInCents;
}
