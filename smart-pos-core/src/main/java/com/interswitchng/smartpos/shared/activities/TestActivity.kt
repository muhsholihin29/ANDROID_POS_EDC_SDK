package com.interswitchng.smartpos.shared.activities

import android.graphics.Typeface
import android.os.Bundle
import android.os.Handler
import android.text.SpannableString
import android.text.style.AbsoluteSizeSpan
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.util.Log
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.interswitchng.smartpos.R
import com.interswitchng.smartpos.shared.interfaces.library.KeyValueStore
import com.interswitchng.smartpos.shared.models.core.CURRENCYTYPE
import com.interswitchng.smartpos.shared.models.core.IswLocal
import com.interswitchng.smartpos.shared.models.core.TerminalInfo
import com.interswitchng.smartpos.shared.models.transaction.IswPaymentInfo
import com.interswitchng.smartpos.shared.models.transaction.cardpaycode.CardType
import com.interswitchng.smartpos.shared.models.transaction.cardpaycode.EmvMessage
import com.interswitchng.smartpos.shared.models.transaction.cardpaycode.request.AccountType
import com.interswitchng.smartpos.shared.models.transaction.cardpaycode.response.TransactionResponse
import com.interswitchng.smartpos.shared.utilities.Logger
import com.interswitchng.smartpos.shared.viewmodel.CardFlowViewModel
import kotlinx.android.synthetic.main.isw_fragment_card_flow.cardPin
import kotlinx.android.synthetic.main.isw_fragment_card_flow.pinHint
import org.koin.android.ext.android.inject
import org.koin.android.viewmodel.ext.android.viewModel


internal class TestActivity : BaseMenuActivity() {

    val cardViewModel: CardFlowViewModel by viewModel()
    // key-value store
    val store: KeyValueStore by inject()
    // terminal info

    private var isCancelled = false
    var cardType = CardType.None
        private set
    var pinOk = false
        private set
    private val logger by lazy { Logger.with("TestActivity") }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_test)

        val terminalInfo = TerminalInfo.get(store)!!
        val textView = findViewById<TextView>(R.id.textView)

        CURRENCYTYPE = IswLocal.INDONESIA.currency
        observeViewModel()
        // setup transaction
        cardViewModel.setupTransaction(10, terminalInfo)
    }

    override fun onStart() {
        super.onStart()
        isCancelled = false
    }

    override fun onDestroy() {
        super.onDestroy()
        isCancelled = true
    }

    private fun readCard() {
        Handler().postDelayed({
            cardViewModel.startTransaction { emvResult, emvData ->
                Log.d("mytag", emvData?.cardTrack2)
                Log.d("mytag", emvData?.icc?.iccAsString)
            }
        }, 10000)
    }

    private fun observeViewModel() {
        with(cardViewModel) {

            // observe emv messages
            emvMessage.observe(this@TestActivity) {
                it?.let(::processMessage)
            }
        }
    }

    fun completeTransaction(response: TransactionResponse) = cardViewModel.completeEmvTransaction(response)


    private fun processMessage(message: EmvMessage) {

        //showCurrencyDialog()
        // assigns value to ensure the when expression is exhausted
        when (message) {

            // when card is detected
            is EmvMessage.CardDetected -> {
                logger.log("Reading Card")
            }

            // when card should be inserted
            is EmvMessage.InsertCard -> {
                logger.log("Insert Card")
            }

            // when card has been read
            is EmvMessage.CardRead -> {

                cardType = message.cardType
                logger.log("CArd Card")
//                parent.onCardRead(message.cardPan)

                // show account type selection
                readCard()
            }

            // when card gets removed
            is EmvMessage.CardRemoved -> {
                logger.log("Card Removed")
            }

            // when user should enter pin
            is EmvMessage.EnterPin -> {
                logger.log("Enter Pin")
            }

            // when user types in pin
            is EmvMessage.PinText -> {
                logger.log("Pin Text")
                cardPin.setText(message.text)
            }

            // when pin has been validated
            is EmvMessage.PinOk -> {
                logger.log("Pin Ok")
                pinOk = true
//                toast("Pin OK")
            }

            // when the user enters an incomplete pin
            is EmvMessage.IncompletePin -> {
                logger.log("Incomplete Pin")
//                alert.setTitle("Invalid Pin")
//                alert.setMessage("Please press the CANCEL (X) button and try again")
//                alert.show()
            }

            // when pin is incorrect
            is EmvMessage.PinError -> {
                logger.log("Pin Error")
//                alert.setTitle("Invalid Pin")
//                alert.setMessage("Please ensure you put the right pin.")
//                alert.show()

                // dismiss alert in 3 seconds
//                Handler().postDelayed({ alert.dismiss() }, 3000).run {
//                    println("posted: $this")
//                }
            }

            // when user cancels transaction
            is EmvMessage.TransactionCancelled -> {
                logger.log("Transaction Cancelled")
            }

            // when transaction is processing
            is EmvMessage.ProcessingTransaction -> {
                logger.log("Processing Transaction")
            }
        }
    }
}