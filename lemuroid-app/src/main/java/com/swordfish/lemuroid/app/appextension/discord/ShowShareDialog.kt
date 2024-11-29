package com.swordfish.lemuroid.app.appextension.discord

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.swordfish.lemuroid.lib.library.db.entity.Game
import com.swordfish.lemuroid.R
import androidx.compose.material3.TextField
import androidx.compose.ui.graphics.Color

@ExperimentalMaterial3Api
@Composable
fun ShowShareDialog(
    game: Game,
    onPositiveClicked: (String, String) -> Unit,
    onDismissRequest: () -> Unit
) {
    val context = LocalContext.current
    val name = remember { mutableStateOf("") }
    val feedback = remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismissRequest) {
        Surface(
            shape = MaterialTheme.shapes.medium,
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth()
            ) {
                Text(
                    text = stringResource(id = R.string.share_discord_popup_description),
                    color = colorResource(id = R.color.textColorAccent),
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(bottom = 24.dp),
                    fontSize = 18.sp
                )

                TextField(
                    value = name.value,
                    onValueChange = { name.value = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    colors = TextFieldDefaults.textFieldColors(
                        focusedIndicatorColor = MaterialTheme.colorScheme.primary,
                        unfocusedIndicatorColor = colorResource(id = R.color.textColorSecondary),
                        focusedTextColor = colorResource(id = R.color.colorWhite),
                        containerColor = Color.Transparent
                    ),
                    placeholder = { Text(text = stringResource(id = R.string.share_discord_dialog_enter_name)) },
                )

                TextField(
                    value = feedback.value,
                    onValueChange = { feedback.value = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    colors = TextFieldDefaults.textFieldColors(
                        focusedIndicatorColor = MaterialTheme.colorScheme.primary,
                        unfocusedIndicatorColor = colorResource(id = R.color.textColorSecondary),
                        focusedTextColor = colorResource(id = R.color.colorWhite),
                        containerColor = Color.Transparent
                    ),
                    placeholder = { Text(text = stringResource(id = R.string.share_discord_dialog_enter_feedback)) },
                )

                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Button(
                        onClick = {
                            if (name.value.isEmpty()) {
                                Toast.makeText(context, "Enter your name!", Toast.LENGTH_SHORT).show()
                            } else if (feedback.value.isEmpty()) {
                                Toast.makeText(context, "Enter your feedback!", Toast.LENGTH_SHORT).show()
                            } else {
                                val shareTextPart1 = context.getString(
                                    R.string.share_discord_text_title_part_1,
                                    name.value,
                                    game.title
                                )

                                val shareText = "$shareTextPart1 ${feedback.value}"
                                onPositiveClicked(
                                    shareText,
                                    game.coverFrontUrl?.replace(" ", "%20").orEmpty()
                                )
                                onDismissRequest()
                            }
                        },
                        modifier = Modifier
                            .padding(top = 16.dp, bottom = 0.dp)
                            .width(150.dp)
                    ) {
                        Text(
                            text = stringResource(id = R.string.share_discord_button_title),
                            color = colorResource(id = R.color.textColorPrimary)
                        )
                    }
                }
            }
        }
    }
}
