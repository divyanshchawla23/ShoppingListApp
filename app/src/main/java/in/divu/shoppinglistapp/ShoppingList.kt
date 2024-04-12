package `in`.divu.shoppinglistapp

import android.Manifest
import android.content.Context
import android.location.Address
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.navigation.NavController

data class  ShoppingItem
    (
    val id: Int ,
    var name :String ,
    var quantity : Int,
    var isedit : Boolean=false,
    var address:String ="")


@Composable
fun ShoppingListApp(
    locationPerm: LocationPerm,
    viewModel: LocationViewModel,
    navController: NavController,
    context: Context,
    address: String

){

    var sItems by remember{ mutableStateOf(listOf<ShoppingItem>()) }

    var showDialogue by remember {
        mutableStateOf(false
        )
    }

    var itemName by remember { mutableStateOf("") }
    var itemquan by remember { mutableStateOf("") }



    val requestpermissionlauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions() ,
        onResult ={ permissions ->
            if(permissions[Manifest.permission.ACCESS_COARSE_LOCATION]==true
                &&
                permissions[Manifest.permission.ACCESS_FINE_LOCATION]==true
            ){

                locationPerm.requestLocationUpdates(viewModel = viewModel)

            }else{
                val rationaleRequired= ActivityCompat.shouldShowRequestPermissionRationale(context as MainActivity,
                    Manifest.permission.ACCESS_FINE_LOCATION)||
                        ActivityCompat.shouldShowRequestPermissionRationale(context as MainActivity,
                            Manifest.permission.ACCESS_FINE_LOCATION
                        )

                if(rationaleRequired){
                    Toast.makeText(context,("Permission required"), Toast.LENGTH_LONG).show()
                }
                else{
                    Toast.makeText(context,("Go to the settings"), Toast.LENGTH_LONG).show()

                }


            }
        } )




    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,


    ) {

        Spacer(modifier = Modifier.height(85.dp))

        Button(onClick = {showDialogue= true},
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .width(200.dp)


        ){
            Text("Add Item")

        }
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ){
            items(sItems){
                item ->
                if(item.isedit){
                    Shoppinglisteditor(item =item , oneditComplete ={
                        editedname,editedqty ->
                        sItems=sItems.map{it.copy(isedit = false)}
                        val editeditem = sItems.find { it.id==item.id }
                        editeditem?.let{
                            it.name=editedname
                            it.quantity=editedqty
                            it.address=address
                        }
                    } )
                }else{
                    Shoppinglistitem(item =item , onEditClick ={
                        sItems=sItems.map{it.copy(isedit = it.id==item.id)}
                    }, onDeleteClick = {
                        sItems=sItems-item
                    })
                }


            }

        }

    }

    if(showDialogue){
        AlertDialog(onDismissRequest = { showDialogue = false },
            confirmButton = {

                Row(modifier = Modifier
                    .fillMaxWidth()
                    .padding(10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween) {
                                Button(onClick = {
                                    if(itemName.isNotBlank()){
                                        val newItem = ShoppingItem(
                                            id = sItems.size+1,
                                            name=itemName,
                                            quantity = itemquan.toInt(),
                                            address=address

                                        )
                                        sItems=sItems+newItem
                                        showDialogue=false
                                        itemName=""
                                        itemquan=""
                                    }
                                }) {


                                    Text("Add")
                                }

                                Button(onClick = { showDialogue=false }) {
                                    Text("Cancel")

                                }

                            }

                            },

            title = { Text("Add shopping item")},
            text = {
                Column {
                    OutlinedTextField(value = itemName,
                        onValueChange = {itemName=it},
                        singleLine = true,
                        label = { Text("Item Name")},
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp))
                    OutlinedTextField(value = itemquan,
                        onValueChange = {itemquan=it},
                        singleLine = true,
                        label = { Text("Quantity")},
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp))

                    Button(onClick = {
                        if(locationPerm.hasLocationPerm(context)){
                            locationPerm.requestLocationUpdates(viewModel)
                            navController.navigate("locationscreen"){
                                this.launchSingleTop
                            }
                        }else{
                            requestpermissionlauncher.launch(arrayOf(
                                Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION

                            ))
                        }
                    }) {
                        Text("Address")
                    }


                }
            }
        )

    }
}



@Composable
fun Shoppinglisteditor(item: ShoppingItem, oneditComplete: (String , Int)-> Unit){
    var editedname by remember { mutableStateOf(item.name) }
    var editedqty by remember { mutableStateOf(item.quantity.toString()) }
    var isediting by remember { mutableStateOf(item.isedit) }



    Row (modifier = Modifier
        .fillMaxWidth()
        .background(Color.Transparent)
        .padding(10.dp),
        horizontalArrangement = Arrangement.SpaceEvenly)

    {
        Column {

            BasicTextField(
                value = editedname,
                onValueChange = {editedname=it},
                singleLine = true,
                modifier = Modifier
                    .wrapContentSize()
                    .padding(10.dp)
            )
            BasicTextField(
                value = editedqty,
                onValueChange = {editedqty=it},
                singleLine = true,
                modifier = Modifier
                    .wrapContentSize()
                    .padding(10.dp)
            )



        }

        Button(
            onClick = {
                isediting=false
                oneditComplete(editedname,editedqty.toIntOrNull() ?: 1)
            }
        ){
            Text("Save")

        }

    }





}


@Composable
fun Shoppinglistitem(
    item: ShoppingItem,
    onEditClick: ()->Unit,
    onDeleteClick: ()->Unit,

){
    Row(
        modifier = Modifier
            .padding(8.dp)
            .fillMaxWidth()
            .border(
                border = BorderStroke(3.dp, Color.Cyan),
                shape = RoundedCornerShape(20)
            ), horizontalArrangement = Arrangement.SpaceBetween
    ) {

        Column (modifier = Modifier
            .weight(1f)
            .padding(8.dp)){
            Row {

                Text(text=item.name, modifier = Modifier.padding(10.dp))
                Text(text="Qty: ${item.quantity}", modifier = Modifier.padding(10.dp))

            }

            Row(modifier = Modifier.fillMaxSize()) {

                IconButton(onClick = { onDeleteClick() }) {
                    Icon(imageVector = Icons.Default.LocationOn, contentDescription = null)


                }

                Text(item.address)

            }



        }


        Row(modifier = Modifier.padding(10.dp)) {
            IconButton(onClick = { onEditClick() }) {
                Icon(imageVector = Icons.Default.Edit, contentDescription = null)

            }
            IconButton(onClick = { onDeleteClick() }) {
                Icon(imageVector = Icons.Default.Delete, contentDescription = null)

            }




        }


    }








}



