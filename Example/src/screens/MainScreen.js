// @flow
import * as React from "react";
import { StyleSheet, ScrollView, View } from "react-native";
import { Router, NavBar, ListItem, Colors } from "../components";
import { TilesScreen } from "./TilesScreen";
import { RNPhotoViewScreen } from "./RNPhotoViewScreen";
import { RNImagePanZoomScreen } from "./RNImagePanZoomScreen";
import { RNFastImageScreen } from "./RNFastImageScreen";
import { BlurScreen } from "./BlurScreen";
import { GradientScreen } from "./GradientScreen";
import { blurFadeIn } from "../transitions";
import { FlatListScreen } from "./FlatListScreen";
import { ListScreen } from "./ListScreen";

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: Colors.back
  },
  content: {
    flex: 1
  }
});

export class MainScreen extends React.Component<{}> {
  render() {
    return (
      <View style={styles.container}>
        <NavBar title="Shared Element Demo" back="none" />
        <ScrollView style={styles.content}>
          <ListItem
            label="Image Move"
            onPress={() =>
              Router.push(<TilesScreen title="Image Move" animation="move" />)
            }
          />
          <ListItem
            label="Image Dissolve"
            onPress={() =>
              Router.push(
                <TilesScreen title="Image Dissolve" animation="dissolve" />
              )
            }
          />
          <ListItem
            label="Image Borderradius"
            onPress={() =>
              Router.push(<ListScreen title="Image Borderradius" />)
            }
          />
          <ListItem
            label="Image & Blur background"
            onPress={() =>
              Router.push(
                <TilesScreen
                  title="Image & Blur"
                  animation="move"
                  DetailComponent={BlurScreen}
                  transitionConfig={blurFadeIn()}
                />
              )
            }
          />
          <ListItem
            label="Gradient overlay"
            onPress={() =>
              Router.push(
                <TilesScreen
                  title="Gradient overlay"
                  DetailComponent={GradientScreen}
                  overlay
                />
              )
            }
          />
          <ListItem
            label="react-native-fast-image"
            onPress={() =>
              Router.push(
                <TilesScreen
                  title="react-native-Fast-image"
                  DetailComponent={RNFastImageScreen}
                />
              )
            }
          />
          <ListItem
            label="react-native-photo-view"
            onPress={() =>
              Router.push(
                <TilesScreen
                  title="react-native-photo-view"
                  DetailComponent={RNPhotoViewScreen}
                />
              )
            }
          />
          <ListItem
            label="react-native-image-pan-zoom"
            onPress={() =>
              Router.push(
                <TilesScreen
                  title="react-native-image-pan-zoom"
                  DetailComponent={RNImagePanZoomScreen}
                />
              )
            }
          />
          <ListItem
            label="FlatList"
            onPress={() => Router.push(<FlatListScreen title="FlatList" />)}
          />
        </ScrollView>
      </View>
    );
  }
}
