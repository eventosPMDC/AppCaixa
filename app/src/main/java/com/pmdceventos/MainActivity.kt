package com.pmdceventos

import android.Manifest.permission
import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.content.PermissionChecker
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
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
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Calendar
import java.util.Locale
import java.util.UUID
import android.view.WindowInsets
import android.view.WindowInsetsController
import android.os.Handler
import android.os.Looper
import android.widget.ImageView
import android.widget.TextView
import kotlin.system.exitProcess
import androidx.core.content.edit
import android.graphics.Color
import androidx.core.graphics.toColorInt

var serialNnbr: String? = ""
var numCx: String? = ""
var vvtg : Double? = 0.00
var cxaberto : String? = ""
var uuidCXDtMov : String? = ""
var cxDtAbMov : String? = ""
var cxDtAbMovCh : String? = ""
var cxHrAbMov : String? = ""
var emFinalizacao : Boolean? = false
var uuidMC : String? = ""
var seqmov : Int? = 0
var crrProd : Boolean? = false
var usaVlrDif : Boolean? = true //Usado para quando for ter valor diferente quanto a finalização, inicialmente para Laura Rebouças quando cartão C/D e Dim/Pix
var showVlrVista: Boolean? = true
var connectedInt: Boolean? = false
private const val REQUEST_CODE_READ_PHONE_STATE = 1

class MainActivity : AppCompatActivity() {
    private lateinit var binding : ActivityMainBinding
    private var db = FirebaseFirestore.getInstance()

    private lateinit var newRecyclerView: RecyclerView
    private lateinit var newArrayList: ArrayList<ItensLista>

    private lateinit var recyclerViewProdutos :RecyclerView
    private lateinit var produtosArrayList: ArrayList<Produto>
    private lateinit var produtosAdapter: AdapterProdutos

    private lateinit var imgStatus: ImageView
    private lateinit var connectivityManager: ConnectivityManager

    private val fechaCaixa = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        result ->
        if (result.resultCode == Activity.RESULT_OK) {
            produtosArrayList.clear()
            produtosAdapter.notifyDataSetChanged()
        }
    }

    private val networkCallback = object : ConnectivityManager.NetworkCallback(){
        override fun onAvailable(network: Network) {
            runOnUiThread {
                imgStatus.setImageResource(R.drawable.wifi_conected)
                connectedInt = true
            }
        }

        override fun onLost(network: Network) {
            runOnUiThread {
                imgStatus.setImageResource(R.drawable.wifi_desconected)
                connectedInt = false
            }
        }
    }

    private val tempoInatividade: Long = 14400000
    private val handler = Handler(Looper.getMainLooper())
    private val inactivityRunnable = Runnable {
        (this as? Activity)?.finishAffinity()
        exitProcess(0)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Thread.sleep(3000)
        installSplashScreen()

        resetInactivityTimer()

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val hasPermission = PermissionChecker.checkSelfPermission(
            this,
            permission.READ_PHONE_STATE
        )

        imgStatus = findViewById(R.id.imgStatus)
        connectivityManager = getSystemService(ConnectivityManager::class.java)
        verificarConexaoInicial()

        this.getConfigsApp(this)
        //serialNnbr = "appEventos".getConfgApp(this)

        if(serialNnbr == ""){
            serialNnbr = UUID.randomUUID().toString()
            //"appEventos".setConfgApp(this, serialNnbr!!)
            this.saveConfigApp(this)
        } else getCaixa(serialNnbr)

        if (hasPermission == PackageManager.PERMISSION_GRANTED) {
            //getCaixa(serialNnbr)
        } else {
            // Solicitar a permissão ao usuário
            //requestPermissions(arrayOf(permission.READ_PHONE_STATE),0)
            ActivityCompat.requestPermissions(this,
                arrayOf(permission.READ_PHONE_STATE),REQUEST_CODE_READ_PHONE_STATE
            )
        }

        /*if (serialNnbr != "") {
            binding.tvText.text = serialNnbr
        }*/

        newRecyclerView = findViewById(R.id.rv_itens)
        newRecyclerView.layoutManager = LinearLayoutManager(this)
        newRecyclerView.setHasFixedSize(true)
        newArrayList = arrayListOf<ItensLista>()

        setClickButton()

        hideSystemBars()

    }

    private fun resetInactivityTimer(){
        handler.removeCallbacks(inactivityRunnable)
        handler.postDelayed(inactivityRunnable,tempoInatividade)
    }

    override fun onUserInteraction() {
        super.onUserInteraction()
        resetInactivityTimer()
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(inactivityRunnable)
    }

    private fun hideSystemBars() {
        // Para Android 11 (API 30) ou superior
        val controller = window.insetsController
        if (controller != null) {
            controller.hide(WindowInsets.Type.systemBars()) // Oculta barra de status e navegação
            controller.systemBarsBehavior = WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
    }

    private fun verificarConexaoInicial(){
        val network = connectivityManager.activeNetwork
        val capabilities = connectivityManager.getNetworkCapabilities(network)
        val conectadoInternet = capabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true

        if (conectadoInternet) {
            imgStatus.setImageResource(R.drawable.wifi_conected)
            connectedInt = true
        } else {
            imgStatus.setImageResource(R.drawable.wifi_desconected)
            connectedInt = false
        }
    }

    override fun onResume() {
        super.onResume()
        connectivityManager.registerDefaultNetworkCallback(networkCallback)
    }

    override fun onPause() {
        super.onPause()
        connectivityManager.unregisterNetworkCallback(networkCallback)
    }

    private fun saveConfigApp(context: Context){
        val sharedPreferences = context.getSharedPreferences("ConfigAppPMDC", MODE_PRIVATE)
        sharedPreferences.edit {
            putString("serialNmb", serialNnbr)
            putString("caixaNmb", numCx)
        }
    }

    private fun getConfigsApp(context: Context){
        val sharedPreferences = context.getSharedPreferences("ConfigAppPMDC", MODE_PRIVATE)
        serialNnbr = sharedPreferences.getString("serialNmb", "")
        numCx = sharedPreferences.getString("caixaNmb","")
        binding.caixa.text = numCx
    }

    private fun String.setConfgApp(context: Context, valor: String){
        val shrPreferences = context.getSharedPreferences("ConfigAppPMDC", MODE_PRIVATE)
        val editor = shrPreferences.edit()
        editor.putString(this,valor)
        editor.apply()
    }

    private fun String.getConfgApp(context: Context): String? {
        val srdPreferences = context.getSharedPreferences("ConfigAppPMDC",Context.MODE_PRIVATE)
        return srdPreferences.getString(this,null)
    }

    private fun geraDados(descricao:String, qtdvlri: String, vlrtt: Double,
                          vlrunt : Double, qtde: Int, idProd: String, vlrunitc : Double) {
        val itensLista = ItensLista(descricao,qtdvlri,vlrtt,vlrunt,qtde, idProd, vlrunitc)
        newArrayList.add(itensLista)
        newRecyclerView.adapter = AdapterItensLista(newArrayList){index -> deleteItem(index)}
        atualizarTotalGeral()
    }

    private fun deleteItem(position :Int){
        //Toast.makeText(this, "item pos $position", Toast.LENGTH_SHORT).show()
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
            if (vvtg != 0.00) {
               return
            }
            val alertDialog = AlertDialog.Builder(ContextThemeWrapper(this, R.style.RoundedAlertDialog))
            val inflater = layoutInflater
            val viewMF = inflater.inflate(R.layout.activity_menu_ferramentas, null)
            alertDialog.setView(viewMF)
            val dialog = alertDialog.create()
            val btnCnfcx = viewMF.findViewById<AppCompatButton>(R.id.ibtn_configcx)
            val btnFchCx = viewMF.findViewById<AppCompatButton>(R.id.ibtn_fechacx)
            val btncbEstque = viewMF.findViewById<AppCompatButton>(R.id.cbEstoque)
            btnCnfcx.setOnClickListener{
                val intent = Intent(this, ConfigCx::class.java)
                intent.putExtra("serialNmbr", serialNnbr)
                if (numCx != "" && numCx != "null") {
                    intent.putExtra("caixa", numCx)
                } else intent.putExtra("caixa","")
                startActivity(intent)
                getCaixa(serialNnbr)
                dialog.dismiss()
            }
            val ibtnAbrirCx = viewMF.findViewById<AppCompatButton>(R.id.ibtn_abrecx)
            ibtnAbrirCx.setOnClickListener {
                if (cxaberto != "fechar" && cxaberto != "true") {
                    abrirCaixa()
                }  else {
                    Toast.makeText(this,"O caixa do dia $cxDtAbMov encontra-se aberto.",Toast.LENGTH_LONG).show()
                }
                dialog.dismiss()
            }
            btnFchCx.setOnClickListener {
                if (cxaberto == "fechar" || cxaberto == "true") {
                    val intent = Intent(this, FechamentoCaixa::class.java)
                    intent.putExtra("dataCX", uuidCXDtMov)
                    intent.putExtra("caixa", numCx)
                    fechaCaixa.launch(intent)
                    //startActivity(intent)
                    //getCaixa(serialNnbr)
                    dialog.dismiss()
                }
            }
            btncbEstque.setOnClickListener {
                if (connectedInt == true) {
                    val intentEstoque = Intent(this, PosicaoEstoque::class.java)
                    startActivity(intentEstoque)
                    dialog.dismiss()
                } else {
                    showCaixaDialogo("Atenção!!", "Você não está conectado na internet, portanto não é possível acessar o estoque!")
                }
            }
            dialog.show()
        }
    }

    private fun getCaixa(srlNmb: String?){
        val rqstCaixa = db.collection("Config").document(srlNmb.toString())
        rqstCaixa.get().addOnSuccessListener {
            if (it != null){
                if (numCx == "" || numCx == "null") {
                    numCx = it.data?.get("caixa").toString()
                    Toast.makeText(this,"Caxia $numCx", Toast.LENGTH_LONG).show()
                    this.saveConfigApp(this)
                    binding.caixa.text = numCx
                }
                cxaberto = it.data?.get("cxaberto").toString()
                if (cxaberto == "true") {
                    cxDtAbMov = it.data?.get("cxDtAbMov").toString()
                    cxDtAbMovCh = cxDtAbMov!!.replace("/","")
                    uuidCXDtMov = it.data?.get("uuidCXDtMov").toString()
                    cxHrAbMov = it.data?.get("cxHrAbMov").toString()
                    seqmov = it.data?.get("seqmov").hashCode()
                    if (validaCxMov(cxDtAbMov.toString(), cxHrAbMov.toString())) {
                        carregarProdutos()
                        crrProd = true
                    } else {
                        cxaberto = "fechar"
                        val dialogBuild = AlertDialog.Builder(this)
                        dialogBuild.setTitle("Sucesso!")
                        dialogBuild.setMessage("O caixa do dia $cxDtAbMov se encontra aberto!\n É necessário fecha-lo e abrir na data de hoje!")
                        dialogBuild.setPositiveButton("Ok"){ dialog, _ -> dialog.dismiss()}
                        val alertDialog = dialogBuild.create()
                        alertDialog.show()
                    }
                }
            }
        }
    }

    private fun carregarProdutos() {
        if (crrProd == false) {
            recyclerViewProdutos = findViewById(R.id.produtos)
            recyclerViewProdutos.layoutManager = LinearLayoutManager(this)
            recyclerViewProdutos.setHasFixedSize(true)
            produtosArrayList = arrayListOf()
            produtosAdapter = AdapterProdutos(produtosArrayList, usaVlrDif == true) { index -> setItemOnList(index) }
            recyclerViewProdutos.adapter = produtosAdapter

            db.collection("Produtos").orderBy("secProd").addSnapshotListener(object : EventListener<QuerySnapshot> {
                override fun onEvent(value: QuerySnapshot?, error: FirebaseFirestoreException?) {
                    if (error != null) {
                        Log.e("Firestore error", error.message.toString())
                        return
                    }
                    for (dc: DocumentChange in value?.documentChanges!!) {
                        if (dc.type == DocumentChange.Type.ADDED) {
                            try {
                                produtosArrayList.add(dc.document.toObject(Produto::class.java))
                            } catch (e: Exception) {
                                Log.e(
                                    "Erro ao acessar 'valor'",
                                    "Exceção ao tentar acessar o campo 'valor': ${e.message}"
                                )
                            }
                        }
                    }
                    produtosAdapter.notifyDataSetChanged()
                }
            })
        }
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
            geraDados(produto.nome,qtdvlri,vlrtotal, produto.valor!!,vlrunit.toInt(), produto.idProd, produto.valorC!!)
        } else {
            val qtdvlr = buildString {
                append("1")
                append(" X ")
                append(formatCurrency(produto.valor))
            }
            geraDados(produto.nome,qtdvlr,produto.valor!!, produto.valor!!,1, produto.idProd, produto.valorC!!)
        }
        binding.tvdisplay.text = "0"
    }

    private fun atualizarTotalGeral(){
        vvtg = 0.00
        var vlrPraso : Double? = 0.00

        for (i in newArrayList.indices){
            vvtg = vvtg!! + newArrayList[i].vlrtotal!!
            vlrPraso = newArrayList[i].qtde?.times(newArrayList[i].vlrUnitC!!)?.let { vlrPraso?.plus(it) }
        }
        if (showVlrVista == true) {
            binding.tvTotalgeral.text = formatCurrency(vvtg)
            binding.tvTotalgeral.setTextColor("#A1A1A0".toColorInt())
        } else {
            binding.tvTotalgeral.text = formatCurrency(vlrPraso)
            binding.tvTotalgeral.setTextColor(Color.RED)
        }
    }

    fun formatCurrency(vlrtotal: Double?): CharSequence? {
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
        binding.dinheiro.setOnClickListener { finalizaVenda(binding.dinheiro.text.toString().trim())}
        binding.cartao.setOnClickListener { finalizaVenda(binding.cartao.text.toString().trim())}
        binding.pix.setOnClickListener { finalizaVenda(binding.pix.text.toString().trim())}
        binding.atualizarVlrTT.setOnClickListener { atualizaVlrTT() }
    }

    private fun atualizaVlrTT(){
        showVlrVista = showVlrVista?.not()
        atualizarTotalGeral()
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
            else {
                    val str :String = binding.tvdisplay.text.toString()
                    binding.tvdisplay.text = str.dropLast(1)
                    if (binding.tvdisplay.length() == 0){
                        binding.tvdisplay.text = "0"
                    }
            }

        }
    }

    private fun finalizaVenda(pagamento: String) {
        if (vvtg != 0.00) {
            if (emFinalizacao == false) {
                emFinalizacao = true
                val calendario = Calendar.getInstance()

                val dia = SimpleDateFormat("dd/MM/yyyy").format(calendario.time)
                val hora = SimpleDateFormat("HH:mm:ss").format(calendario.time)
                seqmov = seqmov!!.plus(1)

                if (usaVlrDif == true && pagamento == "Cartão")  {
                    vvtg = 0.0
                    for (i in newArrayList.indices) {
                        newArrayList[i].vlrtotal = newArrayList[i].qtde?.times(newArrayList[i].vlrUnitC!!)
                        vvtg = vvtg!! + newArrayList[i].vlrtotal!!
                    }
                }

                val movCaixa = hashMapOf(
                    "seqmov" to seqmov,
                    "dia" to dia,
                    "hora" to hora,
                    "caixa" to numCx,
                    "cobranca" to pagamento,
                    "vlrTotal" to vvtg
                )

                val colecaoMovCx = db.collection(numCx!!)
                //val colecaoMovCx = db.collection("movcaixa")
                uuidMC = UUID.randomUUID().toString()

                colecaoMovCx.document(uuidCXDtMov!!)
                    .collection("MovCaixa").document(uuidMC!!).set(movCaixa)
                //colecaoMovCx.document(uuidMC!!).set(movCaixa)

                cxHrAbMov = hora

                val hmUpdConfigCx = hashMapOf(
                    "seqmov" to seqmov,
                    "cxHrAbMov" to cxHrAbMov
                )
                db.collection("Config")
                    .document(serialNnbr!!).update(hmUpdConfigCx as Map<String, Any>)
            }
            var vlrPago : Double = 0.00
            if (binding.tvdisplay.text.toString().toDouble() < vvtg!! &&
                binding.tvdisplay.text.toString().toDouble() != 0.00 &&
                usaVlrDif == false){
                vlrPago = binding.tvdisplay.text.toString().toDouble()
                vvtg = vvtg!! - binding.tvdisplay.text.toString().toDouble()
            } else{
                vlrPago = vvtg!!
                vvtg = 0.00
            }

            //val movCxPgto = db.collection("movcxpagto")
            val movCxPgto = db.collection(numCx!!)
            val movCxPgtoData = hashMapOf(
                "codMovCx" to uuidMC,
                "cobranca" to pagamento,
                "vlrPago" to vlrPago
            )
            movCxPgto.document(uuidCXDtMov!!).collection("MovCxPagto").add(movCxPgtoData)
            if (vvtg!! != 0.00){
                binding.tvTotalgeral.text = formatCurrency(vvtg)
                binding.tvdisplay.text = "0"
                return
            }
            var troco : Double = binding.tvdisplay.text.toString().toDouble()
            if (pagamento == "DINHEIRO" && troco > vlrPago) {
                troco = binding.tvdisplay.text.toString().toDouble()
                troco -= vlrPago
            }

            //val movCxItem = db.collection("movcxitem")
            val movCxItem = db.collection(numCx!!)
            var i : Int =1
            for ((descricao,qtdevlrun,vlrtotal,vlrUnit,qtde,idProd,vlrUnitC) in newArrayList) {
                val movCxItemData = hashMapOf(
                        "codMovCx" to uuidMC,
                        "secItem" to i,
                        "idProd" to idProd,
                        "Produto" to descricao,
                        "VlrUnit" to vlrUnit,
                        "Qtde" to qtde,
                        "VlrUnitC" to vlrUnitC
                    )
                i++
                movCxItem.document(uuidCXDtMov!!).collection("MovCxItem").add(movCxItemData)
                }
            newArrayList.clear()
            newRecyclerView.adapter = AdapterItensLista(newArrayList){index -> deleteItem(index)}
            atualizarTotalGeral()
            emFinalizacao = false
            binding.tvdisplay.text = "0"
            showVlrVista = true
            binding.tvTotalgeral.setTextColor("#A1A1A0".toColorInt())
            if (usaVlrDif == true && pagamento == "Cartão")  {
                showCaixaDialogo("Atenção!!!", "Valor para pagamento em Cartão credito/debito é de ${
                    formatCurrency( vlrPago )
                }")

//                val dialogBuild = AlertDialog.Builder(this)
//                dialogBuild.setTitle("Atenção!!!")
//                dialogBuild.setMessage(
//                    "Valor para pagamento em Cartão credito/debito é de ${
//                        formatCurrency( vlrPago )
//                    }"
//                )
//                dialogBuild.setPositiveButton("Ok") { dialog, _ -> dialog.dismiss()}
//                val alertDialog = dialogBuild.create()
//                alertDialog.show()
            } else {
                if (troco == 0.00) {
                    Toast.makeText(this, "Venda gravada com sucesso!", Toast.LENGTH_LONG).show()
                } else {
                    val dialogBuild = AlertDialog.Builder(this)
                    dialogBuild.setTitle("Atenção!")
                    dialogBuild.setMessage(
                        "Venda gravada com sucesso!\n Troco de ${
                            formatCurrency(
                                troco
                            )
                        }"
                    )
                    dialogBuild.setPositiveButton("Ok") { dialog, _ -> dialog.dismiss() }
                    val alertDialog = dialogBuild.create()
                    alertDialog.show()
                    //Toast.makeText(this,"Venda gravada com sucesso!\n Troco de " +
                    //         "${formatCurrency(troco)}",Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun abrirCaixa() {
        if (cxaberto != "true" ) {
            //validar caixa aberto
            var cxDtAbMovChVld : String? = ""
            val rqstCaixaVld = db.collection("Config").document(serialNnbr!!)
            rqstCaixaVld.get().addOnSuccessListener {
                if (it != null) {
                    cxDtAbMovChVld =it.data?.get("cxDtAbMov").toString()
                }

                val calendario = Calendar.getInstance()
                cxDtAbMov = SimpleDateFormat("dd/MM/yyyy").format(calendario.time)
                cxDtAbMovCh = cxDtAbMov!!.replace("/","")
                cxHrAbMov = SimpleDateFormat("HH:mm:ss").format(calendario.time)
                if (cxDtAbMov != cxDtAbMovChVld) {
                    seqmov = 0
                    uuidCXDtMov = UUID.randomUUID().toString()
                    val abreCx = hashMapOf(
                        "seqmov" to seqmov,
                        "caixa" to numCx,
                        "cobranca" to "ABERTURA DE CAIXA",
                        "dia" to cxDtAbMov,
                        "hora" to cxHrAbMov,
                        "vlrTotal" to 0.00
                    )
                    val idMov = hashMapOf(
                        "idMov" to uuidCXDtMov,
                        "dia" to cxDtAbMov
                    )
                    db.collection(numCx!!).document(uuidCXDtMov!!).set(idMov)
                    db.collection(numCx!!).document(uuidCXDtMov!!).collection("MovCaixa").add(abreCx)
                }
                //    db.collection("movcaixa").document(iddocCx).set(abreCx)
                val rqstCaixa = db.collection("Config").document(serialNnbr.toString())
                rqstCaixa.get()
                cxaberto = "true"
                val config = hashMapOf(
                    "seqmov" to seqmov,
                    "cxDtAbMov" to cxDtAbMov,
                    "cxHrAbMov" to cxHrAbMov,
                    "cxaberto" to cxaberto,
                    "uuidCXDtMov" to uuidCXDtMov
                )
                rqstCaixa.update(config as Map<String, String?>)
                crrProd = false
                carregarProdutos()
            }
            //fim validação

        }
    }

    private fun validaCxMov(dia: String, hora : String): Boolean {
        var resultado : Boolean = false
        if (dia !=  "null" && hora != "null") {
            val calendario = Calendar.getInstance()
            var diahorastr = SimpleDateFormat("dd/MM/yyyy").format(calendario.time)
            diahorastr += " " + SimpleDateFormat("HH:mm:ss").format(calendario.time)
            val dhUltMovstr = dia + " " + hora

            val formato = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")
            val diahora = LocalDateTime.parse(diahorastr, formato)
            val dhUltMov = LocalDateTime.parse(dhUltMovstr, formato)

            val diferencaHoras = ChronoUnit.HOURS.between(dhUltMov, diahora)

            if (diferencaHoras <= 4) {
                resultado = true
            }
        }
        return resultado
    }

    private fun teste(){
        val base = FirebaseFirestore.getInstance()
        base.collection("CAIXA001")
            .document("08052024")
            .collection("MovCaixa")
            .addSnapshotListener(object : EventListener<QuerySnapshot> {
            override fun onEvent(value: QuerySnapshot?, error: FirebaseFirestoreException?) {
                if (error != null) {
                    Log.e("Firestore error", error.message.toString())
                    return
                }
                for (dc: DocumentChange in value?.documentChanges!!) {
                    if (dc.type == DocumentChange.Type.ADDED) {
                        dc.document.id
                        dc.document.getString("seqmov")
                            Log.e(
                                "Erro ao acessar 'valor'",
                                "Exceção ao tentar acessar o campo 'valor': "
                            )
                        }
                }
            }
        })

    }

    fun showCaixaDialogo(titulo: String, mensagem: String) {
        val view = layoutInflater.inflate(R.layout.caixa_dialogo, null)
        view.findViewById<TextView>(R.id.txtTitulo).text = titulo
        view.findViewById<TextView>(R.id.txtMensagem).text = mensagem

        AlertDialog.Builder(this).setView(view).setPositiveButton("OK", null).show()
    }

}