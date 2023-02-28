package com.grayseal.bookshelf.screens.shelf

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.text.HtmlCompat
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import com.grayseal.bookshelf.R
import com.grayseal.bookshelf.model.Book
import com.grayseal.bookshelf.model.MyUser
import com.grayseal.bookshelf.navigation.BookShelfScreens
import com.grayseal.bookshelf.ui.theme.Yellow
import com.grayseal.bookshelf.ui.theme.poppinsFamily

@Composable
fun BooksInShelfScreen(navController: NavController, shelfViewModel: ShelfViewModel, shelfName: String?) {
    /*Get books in this shelf*/
    val user by remember { mutableStateOf(Firebase.auth.currentUser) }
    var booksInShelf by remember {
        mutableStateOf(mutableListOf<Book>())
    }
    val context = LocalContext.current
    val userId = user?.uid

    var loading by remember {
        mutableStateOf(true)
    }

    booksInShelf = shelfViewModel.getBooksInAShelf(userId, context, shelfName.toString(), onDone = {
        loading = false
    })
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(5.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Rounded.Close,
                contentDescription = "Close Icon",
                modifier = Modifier
                    .size(30.dp)
                    .clip(CircleShape)
                    .clickable(enabled = true, onClick = {
                        navController.popBackStack()
                    })
            )
            androidx.compose.material3.Text(
                shelfName.toString(),
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                fontFamily = poppinsFamily,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(start = 30.dp)
            )
        }
        BooksInShelfItems(
            booksInShelf = booksInShelf,
            navController = navController,
            loading = loading
        )
    }
}

@Composable
fun BooksInShelfItems(booksInShelf: List<Book>, navController: NavController, loading: Boolean) {
    if (loading) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            LinearProgressIndicator(color = Yellow)
        }
    } else {
        if (booksInShelf.isNotEmpty()) {
            Spacer(modifier = Modifier.height(30.dp))
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(),
                verticalArrangement = Arrangement.spacedBy(30.dp)
            ) {
                items(items = booksInShelf) { item ->
                    var title = "Title information unavailable"
                    var author = "Author names not on record"
                    var previewText = "Preview information not provided"
                    var imageUrl =
                        "https://media.istockphoto.com/id/1147544807/vector/thumbnail-image-vector-graphic.jpg?s=612x612&w=0&k=20&c=rnCKVbdxqkjlcs3xH87-9gocETqpspHFXu5dIGB4wuM="
                    if (item.imageLinks.toString().isNotEmpty()) {
                        imageUrl = item.imageLinks.thumbnail.toString().trim()
                        imageUrl = imageUrl.replace("http", "https")
                    }
                    if (item.title.isNotEmpty()) {
                        title = item.title
                    }
                    if (item.authors[0].isNotEmpty()) {
                        author = item.authors.joinToString(separator = ", ")
                    }
                    if (item.description.isNotEmpty()) {
                        val cleanDescription =
                            HtmlCompat.fromHtml(item.description, HtmlCompat.FROM_HTML_MODE_LEGACY)
                        previewText = cleanDescription.toString()
                    }
                    val bookId = item.bookID
                    BookCard(
                        bookTitle = title,
                        bookAuthor = author,
                        previewText = previewText,
                        imageUrl = imageUrl,
                        onClick = {
                            navController.navigate(route = BookShelfScreens.BookScreen.name + "/$bookId")
                        }
                    )
                }
            }
        }
        else{
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.nobookinshelf),
                    contentDescription = "Empty Shelf",
                    modifier = Modifier.padding(bottom = 20.dp)
                )
                androidx.compose.material.Text(
                    "Uh oh, no books in the shelf!",
                    fontFamily = poppinsFamily,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                )
            }
        }
    }
}

@Composable
fun BookCard(
    bookTitle: String,
    bookAuthor: String,
    previewText: String,
    imageUrl: String,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .clickable(onClick = onClick)
            .fillMaxWidth(),
        shape = RectangleShape
    ) {
        Row(modifier = Modifier.fillMaxWidth()) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(imageUrl)
                            .build(),
                        contentDescription = "Book Image",
                        contentScale = ContentScale.FillHeight
                    )
                    Column {
                        Text(
                            bookTitle,
                            overflow = TextOverflow.Ellipsis,
                            fontFamily = poppinsFamily,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp,
                            maxLines = 1,
                            color = MaterialTheme.colorScheme.onBackground,
                        )
                        Text(
                            bookAuthor,
                            overflow = TextOverflow.Clip,
                            fontFamily = poppinsFamily,
                            fontSize = 12.sp,
                            maxLines = 1,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                        )
                        Text(
                            previewText,
                            overflow = TextOverflow.Ellipsis,
                            fontFamily = poppinsFamily,
                            fontSize = 13.sp,
                            maxLines = 2,
                            color = MaterialTheme.colorScheme.onBackground,
                        )
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    androidx.compose.material.Icon(
                        Icons.Rounded.RateReview,
                        contentDescription = "Review",
                        tint = Yellow,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(70.dp))
                    androidx.compose.material.Icon(
                        Icons.Rounded.Favorite,
                        contentDescription = "Favourite",
                        tint = Yellow,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(70.dp))
                    androidx.compose.material.Icon(
                        Icons.Rounded.DeleteForever,
                        contentDescription = "Remove",
                        tint = Yellow,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}