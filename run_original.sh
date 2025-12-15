#!/bin/bash
# 运行原始版本（禁用Actor优化）
java -Dactor.disabled=true -cp "build/classes" com.gameengine.example.GameExample
