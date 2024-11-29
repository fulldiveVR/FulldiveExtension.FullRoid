package com.swordfish.lemuroid.app.mobile.feature.proinfo

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.swordfish.lemuroid.R

@Composable
fun FinWizeLayout(
    onClick: () -> Unit,
    onCloseClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 80.dp)
            .background(Color.Transparent)
    ) {
        Card(
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
                .wrapContentHeight()
                .align(Alignment.BottomCenter)
                .clickable { onClick() },
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            colors = CardDefaults.cardColors(containerColor = colorResource(id = R.color.backgroundPopup)),
        ) {
            Row(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_fin_wize_logo),
                    contentDescription = stringResource(R.string.flat_finwize_popup_title),
                    modifier = Modifier.size(40.dp),
                    tint = Color.Unspecified
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(R.string.flat_finwize_popup_title),
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.weight(1f)
                        )
                        Box(
                            modifier = Modifier
                                .clickable { onCloseClick() }
                                .padding(start = 12.dp, bottom = 12.dp)
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.ic_cross),
                                contentDescription = stringResource(R.string.flat_finwize_popup_title),
                                modifier = Modifier.size(12.dp),
                                tint = Color.Unspecified
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = stringResource(R.string.flat_finwize_popup_disclaimer),
                        fontSize = 14.sp,
                        color = Color.White
                    )
                }
            }
        }
    }
}
