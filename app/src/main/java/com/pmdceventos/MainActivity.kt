package com.pmdceventos

import android.Manifest.permission
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.content.PermissionChecker
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.view.ContextThemeWrapper
import androidx.appcompat.widget.AppCompatButton
import androidx.core.app.ActivityCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.EventListener
import com.pmdceventos.databinding.ActivityMainBinding
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Calendar
import java.util.Locale
import java.util.UUID
import kotlin.concurrent.thread

var serialNnbr: String? = ""
var numCx: String? = ""
var vvtg : Double? = 0.00
var cxaberto : String? = ""
var cxDtAbMov : String? = ""
var cxHrAbMov : String? = ""
var emFinalizacao : Boolean? = false
var uuidMC : String? = ""

private const val REQUEST_CODE_READ_PHONE_STATE = 1

class MainActivity : AppCompatActivity() {

    private lateinit var binding : ActivityMainBinding
    private var db = FirebaseFirestore.getInstance()

    private lateinit var newRecyclerView: RecyclerView
    private lateinit var newArrayList: ArrayList<ItensLista>

    private lateinit var recyclerViewProdutos :RecyclerView
    private lateinit var produtosArrayList: ArrayList<Produto>
    private lateinit var produtosAdapter: AdapterProdutos

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Thread.sleep(3000)
        installSplashScreen()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)


        val hasPermission = PermissionChecker.checkSelfPermission(
            this,
            permission.READ_PHONE_STATE
        )

        if (hasPermission == PackageManager.PERMISSION_GRANTED) {
            // TP1A.220624.014
            serialNnbr = Build.ID
            getCaixa(serialNnbr)
        } else {
            // Solicitar a permissão ao usuário
            //requestPermissions(arrayOf(permission.READ_PHONE_STATE),0)
            ActivityCompat.requestPermissions(this,
                arrayOf(permission.READ_PHONE_STATE),REQUEST_CODE_READ_PHONE_STATE
            )
        }
        if (serialNnbr != "") {
            binding.tvText.text = serialNnbr

        }

        newRecyclerView = findViewById(R.id.rv_itens)
        newRecyclerView.layoutManager = LinearLayoutManager(this)
        newRecyclerView.setHasFixedSize(true)
        newArrayList = arrayListOf<ItensLista>()

        setClickButton()
        //carregarProdutos()
    }

    private fun geraDados(descricao:String, qtdvlri: String, vlrtt: Double,
                          vlrunt : Double, qtde: Int, idProd: String) {
        val itensLista = ItensLista(descricao,qtdvlri,vlrtt,vlrunt,qtde, idProd)
        newArrayList.add(itensLista)
        newRecyclerView.adapter = AdapterItensLista(newArrayList){index -> deleteItem(index)}
        atualizarTotalGeral()
    }

    private fun deleteItem(position :Int){
        Toast.makeText(this, "item pos $position", Toast.LENGTH_SHORT).show()
        newArrayList.removeAt(position)
        newRecyclerView.adapter = AdapterItensLista(newArrayList){index -> deleteItem(index)}
        atualizarTotalGeral()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == REQUEST_CODE_READ_PHONE_STATE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // A permissão foi concedida
                // Capturar o número de série
                serialNnbr = Build.ID
                Toast.makeText(
                    this,
                    "Não houve configuração de caixa ainda, por favor fazer a configuração para utilizar o sistema.$serialNnbr",
                    Toast.LENGTH_LONG).show()
                getCaixa(serialNnbr)
            } else {
                // A permissão foi negada
                // Mostrar uma mensagem ao usuário informando que a permissão é necessária
                Toast.makeText(
                    this,
                    "Não foi permitido capturar informações para continuar o processo de configuração!",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    fun showDialog(view: View) {
        if (view.id == R.id.ibtn_config){
            val alertDialog = AlertDialog.Builder(ContextThemeWrapper(this, R.style.RoundedAlertDialog))
            val inflater = layoutInflater
            val viewMF = inflater.inflate(R.layout.activity_menu_ferramentas, null)
            alertDialog.setView(viewMF)
            val dialog = alertDialog.create()
            val btnCnfcx = viewMF.findViewById<AppCompatButton>(R.id.ibtn_configcx)
            btnCnfcx.setOnClickListener{
                val intent = Intent(this, ConfigCx::class.java)
                intent.putExtra("serialNmbr", serialNnbr)
                if (numCx != "") {
                    intent.putExtra("caixa", numCx)
                }
                startActivity(intent)
                dialog.dismiss()
            }
            val ibtnAbrirCx = viewMF.findViewById<AppCompatButton>(R.id.ibtn_abrecx)
            ibtnAbrirCx.setOnClickListener { abrirCaixa() }
            dialog.show()
        }
    }
    private fun getCaixa(srlNmb: String?){
        val rqstCaixa = db.collection("Config").document(srlNmb.toString())
        rqstCaixa.get().addOnSuccessListener {
            if (it != null){
                numCx = it.data?.get("caixa").toString()
                cxaberto = it.data?.get("cxaberto").toString()
                if (cxaberto == "true") {
                    cxDtAbMov = it.data?.get("cxDtAbMov").toString()
                    cxHrAbMov = it.data?.get("cxHrAbMov").toString()
                    if (validaCxMov(cxDtAbMov.toString(), cxHrAbMov.toString())) {
                        carregarProdutos()
                    } else {
                        cxaberto = "false"
                        val dialogBuild = AlertDialog.Builder(this)
                        dialogBuild.setTitle("Sucesso!")
                        dialogBuild.setMessage("O caixa do dia $cxDtAbMov se encontra aberto!/n É necessário fecha-lo e abrir na data de hoje!")
                        dialogBuild.setPositiveButton("Ok"){ dialog, _ -> dialog.dismiss()}
                        val alertDialog = dialogBuild.create()
                        alertDialog.show()
                    }
                }
            }
        }
    }

    private fun carregarProdutos(){
        recyclerViewProdutos = findViewById(R.id.produtos)
        recyclerViewProdutos.layoutManager  = LinearLayoutManager(this)
        recyclerViewProdutos.setHasFixedSize(true)
        produtosArrayList = arrayListOf()
        produtosAdapter = AdapterProdutos(produtosArrayList){index -> setItemOnList(index)}
        recyclerViewProdutos.adapter = produtosAdapter

        db = FirebaseFirestore.getInstance()
        db.collection("Produtos").
               addSnapshotListener(object : EventListener<QuerySnapshot>{
                   override fun onEvent(value: QuerySnapshot?, error: FirebaseFirestoreException?) {

                       if (error != null){
                           Log.e("Firestore error", error.message.toString())
                           return
                       }

                       for (dc : DocumentChange in value?.documentChanges!!){
                           if (dc.type == DocumentChange.Type.ADDED) {
                               try {
                                   produtosArrayList.add(dc.document.toObject(Produto::class.java))
                               } catch (e: Exception) {
                                   Log.e("Erro ao acessar 'valor'", "Exceção ao tentar acessar o campo 'valor': ${e.message}")
                               }
                           }
                       }

                       produtosAdapter.notifyDataSetChanged()

                   }

               })

    }

    private fun setItemOnList(position: Int){
        if (emFinalizacao == true){
            return
        }
        val produto = produtosArrayList[position]
        if(binding.tvdisplay.text != "0"){
            val qtdvlri = buildString {
                append(binding.tvdisplay.text)
                append(" X ")
                append(formatCurrency(produto.valor))
            }
            val vlrunit = binding.tvdisplay.text.toString()
            var vlrtotal = vlrunit.toDouble()
            vlrtotal *= produto.valor!!
            geraDados(produto.nome,qtdvlri,vlrtotal, produto.valor!!,vlrunit.toInt(), produto.idProd)
        } else {
            val qtdvlr = buildString {
                append("1")
                append(" X ")
                append(formatCurrency(produto.valor))
            }
            geraDados(produto.nome,qtdvlr,produto.valor!!, produto.valor!!,1, produto.idProd)
        }
        binding.tvdisplay.text = "0"
    }

    private fun atualizarTotalGeral(){
        vvtg = 0.00

        for (i in newArrayList.indices){
            vvtg = vvtg!! + newArrayList[i].vlrtotal!!
        }
        binding.tvTotalgeral.text = formatCurrency(vvtg)
    }

    private fun formatCurrency(vlrtotal: Double?): CharSequence? {
        val formatoMoeda = NumberFormat.getCurrencyInstance(Locale("pt", "BR"))
        return formatoMoeda.format(vlrtotal)
    }

    private fun setClickButton(){
        binding.btn0.setOnClickListener {setTecladoNum(binding.btn0.text.toString())}
        binding.btn1.setOnClickListener {setTecladoNum(binding.btn1.text.toString())}
        binding.btn2.setOnClickListener {setTecladoNum(binding.btn2.text.toString())}
        binding.btn3.setOnClickListener {setTecladoNum(binding.btn3.text.toString())}
        binding.btn4.setOnClickListener {setTecladoNum(binding.btn4.text.toString())}
        binding.btn5.setOnClickListener {setTecladoNum(binding.btn5.text.toString())}
        binding.btn6.setOnClickListener {setTecladoNum(binding.btn6.text.toString())}
        binding.btn7.setOnClickListener {setTecladoNum(binding.btn7.text.toString())}
        binding.btn8.setOnClickListener {setTecladoNum(binding.btn8.text.toString())}
        binding.btn9.setOnClickListener {setTecladoNum(binding.btn9.text.toString())}
        binding.btnBack.setOnClickListener {setTecladoNum("Voltar")}
        binding.dinheiro.setOnClickListener { finalizaVenda(binding.dinheiro.text.toString())}
        binding.cartao.setOnClickListener { finalizaVenda(binding.cartao.text.toString())}
        binding.pix.setOnClickListener { finalizaVenda(binding.pix.text.toString())}
    }

    private fun setTecladoNum(num : String){
        if (binding.tvdisplay.length() == 1 &&
            binding.tvdisplay.text == "0" && num != "Voltar"){
            binding.tvdisplay.text = ""
            binding.tvdisplay.text = num
        } else {
            if (num != "Voltar"){
                var str :String = binding.tvdisplay.text.toString()
                str = str.plus(num)
                binding.tvdisplay.text = str
            }
            else if (num == "Voltar") {
                    val str :String = binding.tvdisplay.text.toString()
                    binding.tvdisplay.text = str.toString().dropLast(1)
                    if (binding.tvdisplay.length() == 0){
                        binding.tvdisplay.text = "0"
                    }
                }

        }
    }

    private fun finalizaVenda(pagamento: String) {
        if (vvtg != 0.00) {
            if (emFinalizacao == false) {
                val calendario = Calendar.getInstance()
                val dia = SimpleDateFormat("dd/MM/yyyy").format(calendario.time)
                val hora = SimpleDateFormat("HH:mm:ss").format(calendario.time)

                val movCaixa = hashMapOf(
                    "dia" to dia,
                    "hora" to hora,
                    "caixa" to numCx,
                    "cobranca" to pagamento,
                    "vlrTotal" to vvtg
                )

                //val colecaoMovCx = db.collection("MovCaixa")
                val colecaoMovCx = db.collection(numCx!!)
                uuidMC = UUID.randomUUID().toString()
                //colecaoMovCx.document(uuidMC!!).set(movCaixa)
                colecaoMovCx.document(cxDtAbMov!!).collection("MovCaixa").document(uuidMC!!).set(movCaixa)

                emFinalizacao = true
            }
            var vlrPago : Double = 0.00
            if (binding.tvdisplay.text.toString().toDouble() < vvtg!! && binding.tvdisplay.text.toString().toDouble() != 0.00){
                vlrPago = binding.tvdisplay.text.toString().toDouble()
                vvtg = vvtg!! - binding.tvdisplay.text.toString().toDouble()
            } else{
                vlrPago = vvtg!!
                vvtg = 0.00
            }

            //val movCxPgto = db.collection("MovCxPagto")
            val movCxPgto = db.collection(numCx!!)
            val movCxPgtoData = hashMapOf(
                "codMovCx" to uuidMC,
                "cobranca" to pagamento,
                "vlrPago" to vlrPago
            )
            movCxPgto.document(cxDtAbMov!!).collection("MovCxPagto").add(movCxPgtoData)
            if (vvtg!! != 0.00){
                binding.tvTotalgeral.text = formatCurrency(vvtg)
                binding.tvdisplay.text = "0"
                return
            }
            var troco : Double = binding.tvdisplay.text.toString().toDouble()
            if (pagamento == "DINHEIRO" && troco > vlrPago) {
                troco = binding.tvdisplay.text.toString().toDouble()
                troco -= vvtg!!
            }

            //val movCxItem = db.collection("MovCxItem")
            val movCxItem = db.collection(numCx!!)
            val i : Int =1
            for ((descricao,qtdevlrun,vlrtotal,vlrUnit,qtde,idProd) in newArrayList) {
                val movCxItemData = hashMapOf(
                        "codMovCx" to uuidMC,
                        "secItem" to i,
                        "idProd" to idProd,
                        "Produto" to descricao,
                        "VlrUnit" to vlrUnit,
                        "Qtde" to qtde
                    )
                movCxItem.document(cxDtAbMov!!).collection("MovCxItem").add(movCxItemData)
                }
            newArrayList.clear()
            newRecyclerView.adapter = AdapterItensLista(newArrayList){index -> deleteItem(index)}
            atualizarTotalGeral()
            val dialogBuild = AlertDialog.Builder(this)
            emFinalizacao = false
            binding.tvdisplay.text = "0"
            dialogBuild.setTitle("Sucesso!")
            if (troco == 0.00) {
                dialogBuild.setMessage("Venda gravada com sucesso!")
            } else {
                dialogBuild.setMessage("Venda gravada com sucesso!\n Troco de ${formatCurrency(troco)}")
            }
            dialogBuild.setPositiveButton("Ok"){ dialog, _ -> dialog.dismiss()}
            val alertDialog = dialogBuild.create()
            alertDialog.show()
        }
    }

    private fun abrirCaixa() {
        if (cxaberto != "true") {
            val calendario = Calendar.getInstance()
            cxDtAbMov = SimpleDateFormat("dd-MM-yyyy").format(calendario.time)
            cxHrAbMov = SimpleDateFormat("HH:mm:ss").format(calendario.time)
            val abreCx = hashMapOf(
                "caixa" to numCx,
                "cobranca" to "ABERTURA DE CAIXA",
                "dia" to cxDtAbMov,
                "hora" to cxHrAbMov
            )
            db.collection(numCx!!).document(cxDtAbMov!!).collection("MovCaixa").add(abreCx)
            val rqstCaixa = db.collection("Config").document(serialNnbr.toString())
            rqstCaixa.get()
            cxaberto = "true"
            val config = hashMapOf(
                "cxDtAbMov" to cxDtAbMov,
                "cxHrAbMov" to cxHrAbMov,
                "cxaberto" to cxaberto
            )
            rqstCaixa.update(config as Map<String, String?>)
            carregarProdutos()
        }
    }

    private fun validaCxMov(dia: String, hora : String): Boolean {
        var resultado : Boolean = false
        if (dia !=  "null" && hora != "null") {
            val calendario = Calendar.getInstance()
            var diahorastr = SimpleDateFormat("dd-MM-yyyy").format(calendario.time)
            diahorastr += " " + SimpleDateFormat("HH:mm:ss").format(calendario.time)
            val dhUltMovstr = dia + " " + hora

            val formato = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss")
            val diahora = LocalDateTime.parse(diahorastr, formato)
            val dhUltMov = LocalDateTime.parse(dhUltMovstr, formato)

            val diferencaHoras = ChronoUnit.HOURS.between(dhUltMov, diahora)

            if (diferencaHoras <= 4) {
                resultado = true
            }
        }
        return resultado
    }

}