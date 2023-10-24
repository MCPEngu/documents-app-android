package lib.compose.ui.views

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.Checkbox
import androidx.compose.material.CheckboxDefaults
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import lib.compose.ui.enabled
import lib.compose.ui.theme.ManagerTheme
import lib.toolkit.base.R


@Composable
fun AppStepperItem(
    modifier: Modifier = Modifier,
    title: String,
    value: String,
    range: IntRange = 0..Int.MAX_VALUE,
    enabled: Boolean = true,
    dividerVisible: Boolean = true,
    onDownClick: () -> Unit,
    onUpClick: () -> Unit
) {
    AppListItem(
        modifier = modifier,
        title = title,
        dividerVisible = dividerVisible,
        enabled = enabled,
        paddingEnd = 0.dp,
        endContent = {
            StepperItemEndContent(
                enabled = enabled,
                range = range,
                value = value,
                onDownClick = onDownClick,
                onUpClick = onUpClick
            )
        }
    )
}

@Composable
fun AppStepperItem(
    modifier: Modifier = Modifier,
    title: Int,
    value: String,
    range: IntRange = 0..Int.MAX_VALUE,
    enabled: Boolean = true,
    dividerVisible: Boolean = true,
    onDownClick: () -> Unit,
    onUpClick: () -> Unit
) {
    AppStepperItem(
        modifier = modifier,
        title = stringResource(title),
        value = value,
        range = range,
        enabled = enabled,
        dividerVisible = dividerVisible,
        onDownClick = onDownClick,
        onUpClick = onUpClick
    )
}

@Composable
fun CheckableStepperItem(
    modifier: Modifier = Modifier,
    title: String,
    value: String,
    range: IntRange = 0..Int.MAX_VALUE,
    checked: Boolean = true,
    enabled: Boolean = true,
    dividerVisible: Boolean = true,
    onDownClick: () -> Unit,
    onUpClick: () -> Unit,
    onClick: () -> Unit
) {
    Column(modifier = Modifier.clickable { onClick.invoke() }) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(
                checked = checked,
                onCheckedChange = {},
                enabled = enabled,
                colors = CheckboxDefaults.colors(checkedColor = MaterialTheme.colors.primary)
            )
            Text(modifier = Modifier.weight(1f), text = title)
            StepperItemEndContent(
                enabled && checked,
                range,
                if (checked) value else "-",
                onDownClick,
                onUpClick
            )
        }
        AppDivider(startIndent = 16.dp)
    }
}

@Composable
private fun StepperItemEndContent(
    enabled: Boolean,
    range: IntRange,
    value: String,
    onDownClick: () -> Unit,
    onUpClick: () -> Unit,
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        IconButton(
            modifier = Modifier.enabled(enabled && value.toInt() > range.first),
            onClick = onDownClick,
            enabled = enabled && value.toInt() > range.first
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_arrow_down),
                contentDescription = null,
                tint = MaterialTheme.colors.primary,
                modifier = Modifier.enabled(enabled)
            )
        }
        Text(
            modifier = Modifier.widthIn(min = 40.dp),
            text = value,
            style = MaterialTheme.typography.body1,
            color = MaterialTheme.colors.onSurface,
            textAlign = TextAlign.Center
        )
        IconButton(
            modifier = Modifier.enabled(enabled && value.toInt() <= range.last),
            onClick = onUpClick,
            enabled = enabled && value.toInt() < range.last
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_arrow_down),
                contentDescription = null,
                tint = MaterialTheme.colors.primary,
                modifier = Modifier
                    .rotate(180f)
                    .enabled(enabled)
            )
        }
    }
}

@Preview
@Composable
private fun AppStepperItemPreview() {
    ManagerTheme {
        Surface {
            Column {
                CheckableStepperItem(
                    title = stringResource(R.string.app_title),
                    value = "123",
                    checked = true,
                    enabled = true,
                    onDownClick = { },
                    onUpClick = { },
                    onClick = { }
                )
                CheckableStepperItem(
                    title = stringResource(R.string.app_title),
                    value = "123",
                    checked = false,
                    enabled = true,
                    onDownClick = { },
                    onUpClick = { },
                    onClick = { }
                )
                AppStepperItem(title = R.string.app_title, onDownClick = { }, onUpClick = { }, value = "123")
                AppStepperItem(
                    title = R.string.app_title,
                    value = "123",
                    enabled = false,
                    onDownClick = { },
                    onUpClick = { }
                )
            }
        }
    }
}