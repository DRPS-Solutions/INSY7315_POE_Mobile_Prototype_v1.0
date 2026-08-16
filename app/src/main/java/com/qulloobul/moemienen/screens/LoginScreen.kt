@file:OptIn(ExperimentalMaterial3Api::class)

package com.qulloobul.moemienen.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.qulloobul.moemienen.R
import com.qulloobul.moemienen.model.Role

/**
 * Compose equivalent of MainLayout.razor's login card (rendered when
 * currentRole is null). Prototype sign-in: role only, no password yet -
 * same disclaimer as the original.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    isDarkMode: Boolean,
    onToggleDarkMode: () -> Unit,
    onLogin: (name: String, role: Role) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var selectedRole by remember { mutableStateOf<Role?>(null) }
    var roleMenuExpanded by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        IconButton(
            onClick = onToggleDarkMode,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(16.dp)
        ) {
            Icon(
                imageVector = if (isDarkMode) Icons.Filled.LightMode else Icons.Filled.DarkMode,
                contentDescription = if (isDarkMode) "Switch to light mode" else "Switch to dark mode"
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(28.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.logo),
                        contentDescription = "Company Logo",
                        contentScale = ContentScale.Fit,
                        modifier = Modifier.size(72.dp)
                    )

                    Text(
                        text = "QULOOBUL MOE'MIENEEN",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(top = 12.dp)
                    )
                    Text(
                        text = "Sign in to continue",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 4.dp, bottom = 20.dp)
                    )

                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Full name") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp)
                    )

                    ExposedDropdownMenuBox(
                        expanded = roleMenuExpanded,
                        onExpandedChange = { roleMenuExpanded = it },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = selectedRole?.label ?: "",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Role") },
                            trailingIcon = { Icon(Icons.Filled.ArrowDropDown, contentDescription = null) },
                            modifier = Modifier
                                .menuAnchor()
                                .fillMaxWidth()
                        )
                        this.ExposedDropdownMenu(
                            expanded = roleMenuExpanded,
                            onDismissRequest = { roleMenuExpanded = false }
                        ) {
                            Role.entries.forEach { role ->
                                DropdownMenuItem(
                                    text = { Text(role.label) },
                                    onClick = {
                                        selectedRole = role
                                        roleMenuExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    Text(
                        text = selectedRole?.hint ?: "Pick a role to see which tabs it can access.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp)
                    )

                    Button(
                        onClick = { selectedRole?.let { onLogin(name, it) } },
                        enabled = selectedRole != null,
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        contentPadding = PaddingValues(vertical = 12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 20.dp)
                    ) {
                        Text("Log In")
                    }

                    Text(
                        text = "Prototype sign-in - role only, no password yet.",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(top = 16.dp)
                    )
                }
            }
        }
    }
}
