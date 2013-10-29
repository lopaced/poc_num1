# Cards Library: CardGrid

In this page you can find info about:

* [Creating a base CardGrid](#creating-a-base-cardgrid)
* [Use your custom layout for each row](#use-your-custom-layout-for-each-row)


### Creating a base CardGrid

Creating a `CardGridView` is pretty simple.

First, you need an XML layout that will display the `CardListView`.

``` xml
    <it.gmariotti.cardslib.library.view.CardGridView
          android:layout_width="match_parent"
          android:layout_height="match_parent"
          android:columnWidth="190dp"
          android:numColumns="auto_fit"
          android:verticalSpacing="3dp"
          android:horizontalSpacing="2dp"
          android:stretchMode="columnWidth"
          android:gravity="center"
          android:id="@+id/myGrid"/>
```

Then create an Array of `Card`s:

``` java
       ArrayList<Card> cards = new ArrayList<Card>();

      //Create a Card
      Card card = new Card(getContext());

      //Create a CardHeader
      CardHeader header = new CardHeader(getContext());
      ....
      //Add Header to card
      card.addCardHeader(header);

      cards.add(card);
```

Last create a `CardGridArrayAdapter`, get a reference to the `CardGridView` from your code and set your adapter.

``` java
        CardGridArrayAdapter mCardArrayAdapter = new CardGridArrayAdapter(context,cards);

        CardGridView gridView = (CardGridView) getActivity().findViewById(R.id.myGrid);
        if (gridView!=null){
             gridView.setAdapter(mCardArrayAdapter);
        }
```

This `CardGridView` uses for each row the row-list layout `res/layout/list_card_layout.xml`.


### Use your custom layout for each row

Card Library provides 2 built-in row-list layouts.

* `res/layout/list_card_layout.xml`.
* `res/layout/list_card_thumbnail_layout.xml`.

You can customize the layout used for each item in ListView using the attr: `card:list_card_layout_resourceID="@layout/my_layout`

``` xml
       <it.gmariotti.cardslib.library.view.CardGridView
           android:layout_width="match_parent"
           android:layout_height="match_parent"
           android:columnWidth="190dp"
           android:numColumns="auto_fit"
           android:verticalSpacing="3dp"
           android:horizontalSpacing="2dp"
           android:stretchMode="columnWidth"
           android:gravity="center"
           card:list_card_layout_resourceID="@layout/carddemo_grid_gplay"
           android:id="@+id/myGrid"/>
```

In your row-list layout you can use your `CardView` with some its features and its possibilities.
Example `carddemo_grid_gplay.xml`:

``` xml
    <it.gmariotti.cardslib.library.view.CardView
        xmlns:android="http://schemas.android.com/apk/res/android"
        xmlns:card="http://schemas.android.com/apk/res-auto"
        android:id="@+id/list_cardId"
        android:layout_width="190dp"
        android:layout_height="wrap_content"
        style="@style/grid_card"
        card:card_layout_resourceID="@layout/carddemo_googleplay_layout"
        />
```

You can build your layout, but need to have:

 1. a `CardView` with the ID `list_cardId`


This kind of View, doesn't support these `Card` features:

 1. swipe action
 2. collapse/expand action


Currently you have to use the same inner layouts for each card in `CardGridView`



![Screen](https://github.com/gabrielemariotti/cardslib/raw/master/demo/images/demo/grid_gplay.png)